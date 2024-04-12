package com.yupi.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenterbackend.model.domain.UserTeam;
import com.yupi.usercenterbackend.service.UserTeamService;
import com.yupi.usercenterbackend.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 77356
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-01-13 18:21:11
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




