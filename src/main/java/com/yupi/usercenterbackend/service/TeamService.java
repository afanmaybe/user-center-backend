package com.yupi.usercenterbackend.service;

import com.yupi.usercenterbackend.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.usercenterbackend.model.domain.User;
import com.yupi.usercenterbackend.model.dto.TeamQuery;
import com.yupi.usercenterbackend.model.request.TeamAddRequest;
import com.yupi.usercenterbackend.model.request.TeamJoinRequest;
import com.yupi.usercenterbackend.model.request.TeamQuitRequest;
import com.yupi.usercenterbackend.model.request.TeamUpdateRequest;
import com.yupi.usercenterbackend.model.vo.TeamUserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author 77356
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-01-13 17:39:54
*/
public interface TeamService extends IService<Team> {

    long addTeam(Team team, User loginUser);

    List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin);

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);

    boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser);

    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    boolean deleteTeam(long id, User loginUser);
}
