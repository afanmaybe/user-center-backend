package com.yupi.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenterbackend.common.ErrorCode;
import com.yupi.usercenterbackend.constant.UserConstant;
import com.yupi.usercenterbackend.enums.TeamStatusEnum;
import com.yupi.usercenterbackend.exception.BusinessException;
import com.yupi.usercenterbackend.model.domain.Team;
import com.yupi.usercenterbackend.model.domain.User;
import com.yupi.usercenterbackend.model.domain.UserTeam;
import com.yupi.usercenterbackend.model.dto.TeamQuery;
import com.yupi.usercenterbackend.model.request.TeamJoinRequest;
import com.yupi.usercenterbackend.model.request.TeamQuitRequest;
import com.yupi.usercenterbackend.model.request.TeamUpdateRequest;
import com.yupi.usercenterbackend.model.vo.TeamUserVO;
import com.yupi.usercenterbackend.model.vo.UserVO;
import com.yupi.usercenterbackend.service.TeamService;
import com.yupi.usercenterbackend.mapper.TeamMapper;
import com.yupi.usercenterbackend.service.UserService;
import com.yupi.usercenterbackend.service.UserTeamService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
* @author 77356
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-01-13 17:39:54
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public long addTeam(Team team, User loginUser) {
        //1 请求参数是否为空？
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2 是否登录，未登录不允许创建
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final long userId = loginUser.getId();
        //3 校验信息
            //a 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if(maxNum<1 || maxNum>20 ){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"队伍人数有误");
        }
            //b 队伍标题 <= 20
        String teamName = team.getName();
        if(StringUtils.isBlank(teamName) || teamName.length() >20){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"队伍标题有误");
        }
            //c 描述 <= 512
        String description = team.getDescription();
        if(StringUtils.isNotBlank(description) &&description.length() >512){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"队伍描述有误");
        }
            //d status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if(teamStatusEnum == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"队伍状态有误");
        }
            //e 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if(StringUtils.isBlank(password) || password.length() > 32 ){
                throw  new BusinessException(ErrorCode.SYSTEM_ERROR,"队伍密码有误");
            }
        }
            //f 超时时间 > 当前时间
        if(new Date().after(team.getExpireTime())){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"队伍超时时间有误");
        }
            //g 校验用户最多创建 5 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper();
        queryWrapper.eq("userId",userId);
        long count = this.count(queryWrapper);
        if(count < 0 ||  count > 5){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"用户最多创建5个队伍");
        }
        //4 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if(!result || teamId == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        //5 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin) {
        //1 从请求参数中取出队伍名称等查询条件，如果存在则作为查询条件
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if(queryWrapper != null){
            Long id = teamQuery.getId();
            if(id != null && id > 0){
                queryWrapper.eq("id",id);
            }
            //可以通过某个关键词同时对名称和描述查询
            String searchText = teamQuery.getSearchText();
            if(StringUtils.isNotBlank(searchText)){
                queryWrapper.and(qw->qw.like("name",searchText).or().like("description",searchText));
            }
            String name = teamQuery.getName();
            if(StringUtils.isNotBlank(name)){
                queryWrapper.like("name",name);
            }
            String description = teamQuery.getDescription();
            if(StringUtils.isNotBlank(description)){
                queryWrapper.like("description",description);
            }
            Long userId = teamQuery.getUserId();
            if(userId != null && userId > 0){
                queryWrapper.eq("userId",userId);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if(maxNum != null && maxNum > 0){
                queryWrapper.eq("maxNum",maxNum);
            }
            //根据状态来查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if(statusEnum == null){
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            //只有管理员才能查看加密还有非公开的房间
            if(!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status",statusEnum.getValue());
        }

        //不展示已过期的队伍（根据过期时间筛选）
        queryWrapper.and(qw-> qw.gt("expireTime",new Date()).or().isNull("expireTime"));

        List<Team> teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        //添加创建人信息（关联查询）
        List<TeamUserVO> teamUserVOList = new ArrayList<TeamUserVO>();
        for(Team team : teamList){
            Long userId = team.getUserId();
            if(userId == null){
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            if(user != null){
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user,userVO);//脱敏
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser) {
        //1 判断请求参数是否为空
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2 查询队伍是否存在
        Long id = teamUpdateRequest.getId();
        if(id == null || id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if(oldTeam == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        //3 只有管理员或者队伍的创建者可以修改
        int userRole = Optional.ofNullable(loginUser.getUserRole()).orElse(0);
        if(UserConstant.ADMIN_ROLE != userRole || oldTeam.getUserId() != loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        //4 如果用户传入的新值和老值一致，就不用 update 了（可自行实现，降低数据库使用次数）TODO

        //5 如果队伍状态改为加密，必须要有密码
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if(TeamStatusEnum.SECRET.equals(statusEnum)){
            if(StringUtils.isBlank(teamUpdateRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密房间必须设置密码");
            }
        }

        //6 更新成功
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest,updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser) {

        //2队伍必须存在，只能加入未满、未过期的队伍
        Long teamId = teamJoinRequest.getTeamId();
        if(teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        Date expireTime = team.getExpireTime();
        if(expireTime != null && expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已过期");
        }

        //4禁止加入私有的队伍
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(team.getStatus());
        if(TeamStatusEnum.PRIVATE.equals(statusEnum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"禁止加入私有队伍");
        }
        //5如果加入的队伍是加密的，必须密码匹配才可以
        String password = teamJoinRequest.getPassword();
        if(TeamStatusEnum.SECRET.equals(statusEnum)){
            if(StringUtils.isBlank(password) || !team.getPassword().equals(password)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码有误");
            }
        }

        //1用户最多加入 5 个队伍
        long userId = loginUser.getId();
        if(userId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 只有一个线程能获得锁
        RLock lock = redissonClient.getLock("yupao:join_team");//todo 可以放在常量类中
        while(true){
            try {
                if(lock.tryLock(0, -1, TimeUnit.MILLISECONDS)){
                    System.out.println("getLock: " + Thread.currentThread().getId());
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId",userId);
                    long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if(hasJoinNum > 5){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多加入5个队伍");
                    }
                    //只能加入未满队伍
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("teamId",teamId);
                    long hasJoinTeamNum = userTeamService.count(userTeamQueryWrapper);
                    if(hasJoinTeamNum >= team.getMaxNum()){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已满");
                    }
                    //不能重复加入已加入的队伍
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId",userId);
                    userTeamQueryWrapper.eq("teamId",teamId);
                    long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if (hasUserJoinTeam > 0){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已加入该队伍");
                    }
                    //6新增队伍 - 用户关联信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            } catch (InterruptedException e) {
                log.error("doCacheRecommendUser error", e);
                return false;
            } finally {
                // 只能释放自己的锁
                if (lock.isHeldByCurrentThread()) {
                    System.out.println("unLock: " + Thread.currentThread().getId());
                    lock.unlock();
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        //1 校验请求参数
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2 校验队伍是否存在
        Long teamId = teamQuitRequest.getTeamId();
        if(teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        //3 校验我是否已加入队伍
        long userId = loginUser.getId();
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>(userTeam);
        long hasJoinTeam = userTeamService.count(userTeamQueryWrapper);
        if(hasJoinTeam == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未加入队伍");
        }
        //4 如果队伍
        //a 只剩一人，队伍解散
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        long hasJoinTeamNum = userTeamService.count(userTeamQueryWrapper);
        if(hasJoinTeamNum == 1){
            //删除队伍
            this.removeById(teamId);
        }else {
            //b 还有其他人
            //ⅰ 如果是队长退出队伍，权限转移给第二早加入的用户 —— 先来后到
            // 1. 查询已加入队伍的所有用户和加入时间（只用取 id 最小的 2 条数据）
            if(team.getUserId() == userId){
                userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId",teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeams = userTeamService.list(userTeamQueryWrapper);
                if(CollectionUtils.isEmpty(userTeams) || userTeams.size() <= 0){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeams.get(1);
                Long nextUserTeamUserId = nextUserTeam.getUserId();
                //更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextUserTeamUserId);
                boolean result = this.updateById(updateTeam);
                if(!result){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队伍队长失败");
                }
            }
            //ⅱ.  非队长，自己退出队伍
        }
        //移除关系
        return userTeamService.remove(userTeamQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        //1校验请求参数
        //2校验队伍是否存在
        Team team = this.getById(id);
        Long teamId = team.getId();
        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        //3校验你是不是队伍的队长
        long userId = loginUser.getId();
        if(team.getUserId() != userId){
            throw new BusinessException(ErrorCode.NO_AUTH,"无权限");
        }
        //4移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍关联信息失败");
        }
        //5删除队伍
        return this.removeById(id);
    }


}




