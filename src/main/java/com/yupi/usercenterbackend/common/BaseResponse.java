package com.yupi.usercenterbackend.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 通用返回类
 * @author: afan
 * @create: 2023/12/27 22:22
 */
@Data
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 8762268501928252214L;

    private int code;
    private T data;
    private String message;
    private String description;

    public BaseResponse(int code, T data, String message,String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(int code, T data, String message) {
        this(code,data,message,"");
    }

    public BaseResponse(int code, T data) {
        this(code,data,"","");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(),null,errorCode.getMessage(),errorCode.getDescription());
    }
}
