package com.iths.redis.cache;

/**
 * @author sen.huang
 * @date 2019/1/28.
 */
public interface Cache {
    /**
     *
     * 添加-有默认超时时间
     * @param key
     * @param value
     */
    void put(String key, Object value);

    /**
     * 指定超时时间
     * @param key
     * @param value
     * @param expire  超时时间(s）
     */
    void put(String key, Object value,long expire);

    /**
     * 持久化
     * @param key
     * @param value
     */
    void persist(String key, Object value);

    /**
     * 加锁持久化
     * 添加
     * @param key
     * @param value
     */
    void putLock(String key, Object value);

    /**
     * 加锁持久化指定超时时间
     * 添加-指定超时时间
     * @param key
     * @param value
     * @param expire  超时时间(s）
     */
    void putLock(String key, Object value,long expire);


    /**
     * 加锁持久化
     * 添加
     * @param key
     * @param value
     */
    void persistLock(String key, Object value);

    /**
     * 移除key
     * @param key
     * @return
     */
    void remove(String key);

    /**
     * 得到key的值
     * @param key
     * @return
     */
    <T> T get(String key);

}
