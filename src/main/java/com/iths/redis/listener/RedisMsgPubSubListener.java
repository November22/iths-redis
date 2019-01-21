package com.iths.redis.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author sen.huang
 * @date 2019/1/21.
 */
public class RedisMsgPubSubListener implements MessageListener {

    @Autowired
    private RedisTemplate redisTemplate;

    public void onMessage(Message message, byte[] pattern) {
        RedisSerializer<?> serializer = redisTemplate.getValueSerializer();
        System.out.println("通道匹配的模式:{"+ new String(pattern)+"}" );
        System.out.println("消息: {"+serializer.deserialize(message.getBody())+"}");
        System.out.println("通道{"+ redisTemplate.getStringSerializer().deserialize(message.getChannel())+"}");
        System.out.println(message);
    }
}
