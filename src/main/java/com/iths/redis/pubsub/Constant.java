package com.iths.redis.pubsub;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sen.huang
 * @date 2019/1/26.
 */
public class Constant {

    public static final String TOPIC_PREFIX = "redis.";

    public static Map<String,MessageHandler> handleMap = new HashMap<String,MessageHandler>();
}
