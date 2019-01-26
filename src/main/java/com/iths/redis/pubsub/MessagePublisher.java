package com.iths.redis.pubsub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 消息发布
 * @author sen.huang
 * @date 2019/1/26.
 */
public class MessagePublisher {

    //@Autowired 此处不能自动注入，是因为静态变量不是一个对象的属性，而是一个类的属性
    private static RedisTemplate redisTemplate;

    /**
     * 静态注入
     * @param redisTemplate
     */
    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate){
        MessagePublisher.redisTemplate = redisTemplate;
    }

    /**
     * 发布消息
     * @param topic
     * @param content
     */
    public static void publish(String topic,Object content){
        topic = Constant.TOPIC_PREFIX+topic;
        MessageExt messageExt = new MessageExt();
        messageExt.setTopic(topic);
        messageExt.setContent(content);
        redisTemplate.convertAndSend(topic,messageExt);
    }
}
