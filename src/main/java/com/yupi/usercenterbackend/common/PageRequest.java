package com.yupi.usercenterbackend.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description:
 * @author: afan
 * @create: 2024/1/13 23:08
 */
@Data
public class PageRequest implements Serializable {


    private static final long serialVersionUID = -4273860148713910004L;

    /**
     * 页面大小
     */
    protected int pageSize;

    /**
     * 当前第几页
     */
    protected int pageNum = 1;
}
