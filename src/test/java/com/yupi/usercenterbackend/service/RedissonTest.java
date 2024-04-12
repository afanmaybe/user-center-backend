package com.yupi.usercenterbackend.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: afan
 * @create: 2024/1/12 1:00
 */
@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void testRedisson(){
        //list
        List<String> list = new ArrayList<>();
        list.add("zhangsan");
        System.out.println("list：" + list.get(0));
        //list.remove(0);

        //Rlist
        RList<String> rList = redissonClient.getList("test-list");
        //rList.add("zhangsan");
        System.out.println("rlist：" + rList.get(0));
        rList.remove(0);

        //map

    }
}
