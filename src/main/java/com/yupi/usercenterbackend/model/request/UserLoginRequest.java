package com.yupi.usercenterbackend.model.request;

import lombok.Data;
import java.io.Serializable;

/**
 * @description:
 * @author: afan
 * @create: 2023/12/22 17:51
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 5900605140461510895L;

    private String userAccount;
    private String userPassword;
}
