package com.iths.redis.cache;

import com.iths.redis.cache.impl.CacheImpl;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author sen.huang
 * @date 2019/1/29.
 */
public class CacheFactory implements FactoryBean<Cache> {

    private RedisTemplate redisTemplate;

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Cache getObject() throws Exception {
        return new CacheImpl(redisTemplate);
    }

    public Class<?> getObjectType() {
        return Cache.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
