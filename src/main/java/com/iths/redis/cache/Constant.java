package com.iths.redis.cache;

/**
 * @author sen.huang
 * @date 2019/1/29.
 */
public class Constant {
    /**
     * 缓存默认超时时间ms
     */
    public static final Long REDIS_DEFAULT_EXPIRE_TIME = 5*60*1000L;

    /**
     * 锁存储的超时时间ms
     */
    public static final Long REDIS_LOCK_DEFAULT_EXPIRE_TIME = 1000L;

    /**
     * 锁等的超时时间
     */
    public static final Long REDIS_LOCK_DEFAULT_WAIT_TIME = 2*1000L;
}
