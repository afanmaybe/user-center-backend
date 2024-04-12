package com.yupi.usercenterbackend.service;

import com.yupi.usercenterbackend.mapper.UserMapper;
import com.yupi.usercenterbackend.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @description:
 * @author: afan
 * @create: 2024/1/9 11:52
 */
@SpringBootTest
public class InsertUsersTest {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    //设置线程
    private ExecutorService executorService = new ThreadPoolExecutor(16, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    //打包时需要忽略，否则又会跑一次插入数据

    /**
     * 单次插入
     */
    @Test
    public void doInsertUsers01(){
        long start = System.currentTimeMillis();
        final int INSERT_NUM = 1000;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假张三");
            user.setUserAccount("zhangsan");
            user.setAvatarUrl("");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("");
            user.setEmail("");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("111");
            user.setTags("[]");
            userMapper.insert(user);
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);//34717
    }

    /**
     * 批量插入
     */
    @Test
    public void doInsertUsers02(){
        long start = System.currentTimeMillis();
        final int INSERT_NUM = 1000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假张三");
            user.setUserAccount("zhangsan");
            user.setAvatarUrl("");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("");
            user.setEmail("");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("111");
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList,100);
        long end = System.currentTimeMillis();
        System.out.println(end - start);//1521
    }

    /**
     * 并发批量插入用户
     */
    @Test
    public void doInsertUsers03(){
        long start = System.currentTimeMillis();
        final int INSERT_NUM = 100000;
        //分十组
        int j = 0;
        //批量插入数据的大小
        int batchSize = 5000;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM/batchSize; i++) {
            List<User> userList = new ArrayList<>();
            while(true){
                j++;
                User user = new User();
                user.setUsername("假张三");
                user.setUserAccount("zhangsan");
                user.setAvatarUrl("");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("");
                user.setEmail("");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("111");
                user.setTags("[]");
                userList.add(user);
                if(j % 1000 == 0){
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                userService.saveBatch(userList, 1000);//异步方式无需等待此处执行完毕，可以单开线程进入下一次循环，
            },executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}
