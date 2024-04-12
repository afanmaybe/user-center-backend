package com.yupi.usercenterbackend.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description:
 * @author: afan
 * @create: 2024/1/18 11:36
 */
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = 3740732536610742092L;
    /**
     * teamId
     */
    private Long teamId;

}
