package com.yupi.usercenterbackend.once;

import com.yupi.usercenterbackend.mapper.UserMapper;
import com.yupi.usercenterbackend.model.domain.User;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: afan
 * @create: 2024/1/9 11:38
 */
@Component
public class InsertUsers {

    @Resource
    private UserMapper userMapper;

    //在单元测试了
    public void doInsertUsers(){
        User user = new User();
    }
}
