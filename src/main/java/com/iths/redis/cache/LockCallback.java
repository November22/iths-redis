package com.iths.redis.cache;

/**
 * 获得锁后的处理器
 * @author sen.huang
 * @date 2019/1/30.
 */
public interface LockCallback {
    void process();
}
