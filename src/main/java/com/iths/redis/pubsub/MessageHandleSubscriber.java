package com.iths.redis.pubsub;

/**
 * 订阅工具
 * @author sen.huang
 * @date 2019/1/26.
 */
public class MessageHandleSubscriber {
    /**
     * 注册监听消息处理器
     * @param topic
     * @param messageHandler
     */
    public static void subscribe(String topic, MessageHandler messageHandler){
        topic = Constant.TOPIC_PREFIX + topic;
        if(Constant.handleMap.containsKey(topic)){
            //@TODO 日志输出
        }
        Constant.handleMap.put(topic,messageHandler);
    }
}
