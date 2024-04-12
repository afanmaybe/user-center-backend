package com.yupi.usercenterbackend.service;

import com.yupi.usercenterbackend.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * @description:
 * @author: afan
 * @create: 2024/1/10 11:14
 */
@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 配置自定义序列化后，则按照配置方式执行。未配置则是原生jdk序列化。
     */
    @Test
    void testRedis01(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 增
        valueOperations.set("zhangsanString","zhangsan");
        valueOperations.set("zhangsanInteger",1);
        valueOperations.set("zhangsanDouble",1.1);
        User user = new User();
        user.setId(1L);
        user.setUsername("zhangsan");
        valueOperations.set("zhangsanUser",user);
        // 查
        Object zhangsan = valueOperations.get("zhangsanString");
        Assertions.assertTrue("zhangsan".equals((String) zhangsan));
        zhangsan = valueOperations.get("zhangsanInteger");
        Assertions.assertTrue(1 == (Integer) zhangsan);
        zhangsan = valueOperations.get("zhangsanDouble");
        Assertions.assertTrue(1.1 == (Double) zhangsan);
        System.out.println(valueOperations.get("zhangsanUser"));
    }

    @Test
    void testRedis02(){
        ValueOperations valueOperations = stringRedisTemplate.opsForValue();
        // 增
        valueOperations.set("zhangsanString","zhangsan");
        // 查
        Object zhangsanString = valueOperations.get("zhangsanString");
        Assertions.assertTrue("zhangsan".equals((String) zhangsanString));
    }


}
