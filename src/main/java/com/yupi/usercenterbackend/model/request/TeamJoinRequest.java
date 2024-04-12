package com.yupi.usercenterbackend.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @description:
 * @author: afan
 * @create: 2024/1/17 21:08
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = 3999786173363051683L;

    /**
     * teamId
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}
