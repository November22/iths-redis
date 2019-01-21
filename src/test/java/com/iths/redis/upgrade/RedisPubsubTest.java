package com.iths.redis.upgrade;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author sen.huang
 * @date 2019/1/21.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-redis.xml"})
public class RedisPubsubTest  {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testPub(){
        redisTemplate.convertAndSend("redis.haha","9527哇啊哈哈");
    }
}
