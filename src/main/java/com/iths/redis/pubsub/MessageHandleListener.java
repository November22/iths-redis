package com.iths.redis.pubsub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 消息监听器
 * @author sen.huang
 * @date 2019/1/21.
 */
public class MessageHandleListener implements MessageListener {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 监听消息-这个方法接收方是异步的
     * @param message
     * @param pattern
     */
    public void onMessage(Message message, byte[] pattern) {
        MessageExt messageExt = (MessageExt)redisTemplate.getValueSerializer().deserialize(message.getBody());
        System.out.println(messageExt.getTopic());
        if(Constant.handleMap.containsKey(messageExt.getTopic())){
            MessageHandler messageHandler = Constant.handleMap.get(messageExt.getTopic());
            //反序列化data
            messageHandler.handle(messageExt.getContent());
        }
    }
}
