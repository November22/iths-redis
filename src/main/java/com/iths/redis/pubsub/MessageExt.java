package com.iths.redis.pubsub;

import java.util.HashMap;

/**
 * 消息传输
 * @author sen.huang
 * @date 2019/1/26.
 */
public class MessageExt extends HashMap {

    private static final long serialVersionUID = -8398674103830947028L;

    private static final String TOPIC_KEY = "m.topic";

    private static final String CONTENT_KEY = "m.body";

    /**
     * 获取topic主题
     */
    public String getTopic() {
        Object topicObj = this.get(TOPIC_KEY);
        if (topicObj == null) {
            return null;
        } else {
            return (String) topicObj;
        }
    }

    /**
     * 设置topic主题
     */
    public void setTopic(String topic) {
        put(TOPIC_KEY, topic);
    }

    /**
     * 获取内容
     */
    public Object getContent() {
        Object contentObj = this.get(CONTENT_KEY);
        if (contentObj == null) {
            return null;
        } else {
            return contentObj;
        }
    }

    /**
     * 设置内容
     */
    public void setContent(Object content) {
        put(CONTENT_KEY, content);
    }
}
