package com.iths.redis.base;

import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * @author sen.huang
 * @date 2019/1/20.
 */
public class RedisPubSubTest {

    @Test
    public void testSubscribe(){
        Jedis jedis = new Jedis("redis.yhj.3g",6379);
        jedis.auth("yhj1q@W");
        jedis.subscribe(new RedisMsgPubSubListener(),"CCTV1","CCTV2");
    }

    @Test
    public void testPublish(){
        Jedis jedis = new Jedis("redis.yhj.3g",6379);
        jedis.auth("yhj1q@W");
        Long subscribeLong = jedis.publish("CCTV1", "2019猪");
        System.out.println("监听者数量："+subscribeLong);
    }

    @Test
    public void testUnSubscribe(){

    }
}
