package com.iths.redis.cache.impl;

import com.iths.redis.cache.Cache;
import com.iths.redis.cache.Constant;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.List;

/**
 * @author sen.huang
 * @date 2019/1/29.
 */
public class CacheImpl implements Cache {

    private RedisTemplate redisTemplate;

    private RedisSerializer keySerializer;

    private RedisSerializer valueSerializer;

    public CacheImpl(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.keySerializer = redisTemplate.getKeySerializer();
        this.valueSerializer = redisTemplate.getValueSerializer();
    }



    public void put(final String key, final Object value) {
        this.put(key,value,Constant.REDIS_DEFAULT_EXPIRE_TIME);
    }

    public void put(final String key, final Object value,final long expire) {
        redisTemplate.execute(new RedisCallback() {
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                //虽然key被序列化了，但是在redis中，key以字符串形式存储
                byte[] kb = keySerializer.serialize(key);
                byte[] vb = valueSerializer.serialize(value);
                redisConnection.setEx(kb,expire,vb);
                return null;
            }
        });
    }

    public void persist(final String key, final Object value) {
        redisTemplate.execute(new RedisCallback() {
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] kb = keySerializer.serialize(key);
                byte[] vb = valueSerializer.serialize(value);
                //不设置超时时间，就永不超时
                redisConnection.set(kb,vb);
                return null;
            }
        });
    }

    public void putLock(final String key, final Object value) {
        this.putLock(key,value,Constant.REDIS_DEFAULT_EXPIRE_TIME);
    }

    public void putLock(final String key, final Object value, final long expire) {
        redisTemplate.execute(new RedisCallback() {
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] kb = keySerializer.serialize(key);
                byte[] vb = valueSerializer.serialize(value);
                //关注
                redisConnection.watch(kb);
                //开始事务
                redisConnection.multi();
                //测试事务+watch
//                try {
//                    System.out.println("睡眠开始["+System.currentTimeMillis()+"]");
//                    Thread.sleep(30*1000L);
//                    System.out.println("睡眠结束["+System.currentTimeMillis()+"]");
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                redisConnection.setEx(kb,expire,vb);
                List<Object> exec = redisConnection.exec();
                //@TODO 日志输出
                System.out.println(exec);
                return null;
            }
        });
    }

    public void persistLock(final String key, final Object value) {
        redisTemplate.execute(new RedisCallback() {
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] kb = keySerializer.serialize(key);
                byte[] vb = valueSerializer.serialize(value);
                //关注
                redisConnection.watch(kb);
                //开始事务
                redisConnection.multi();
                redisConnection.set(kb,vb);
                List<Object> exec = redisConnection.exec();
                //@TODO 日志输出
                System.out.println(exec);
                return null;
            }
        });
    }

    public void remove(final String key) {
        redisTemplate.execute(new RedisCallback() {
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                if(key == null){
                    return null;
                }
                byte[] kb = keySerializer.serialize(key);
                //1 = 删除成功，0 = 删除失败，应该是数不存在
                Long del = redisConnection.del(kb);
                System.out.println(del);
                return null;
            }
        });
    }

    public <T> T get(final String key) {
        return (T)redisTemplate.execute(new RedisCallback<T>() {
            public T doInRedis(RedisConnection redisConnection) throws DataAccessException {
                if(key == null){
                    return null;
                }
                byte[] kb = keySerializer.serialize(key);
                byte[] vb = redisConnection.get(kb);
                if(vb != null && vb.length > 0){
                    Object deserialize = valueSerializer.deserialize(vb);
                    return (T)deserialize;
                }
                return null;
            }
        });

    }
}
