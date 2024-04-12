package com.yupi.usercenterbackend.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: afan
 * @create: 2023/12/22 17:35
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 2185411374431478614L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String planetCode;

}
