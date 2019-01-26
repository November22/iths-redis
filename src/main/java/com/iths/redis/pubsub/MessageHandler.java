package com.iths.redis.pubsub;

/**
 * sub方接收到消息后处理
 * @author sen.huang
 * @date 2019/1/26.
 */
public interface MessageHandler {

    /**
     * @param value 监听的值
     */
    public void handle(Object value);

}
