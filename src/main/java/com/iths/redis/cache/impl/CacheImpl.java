package com.iths.redis.cache.impl;

import com.iths.redis.cache.Cache;
import com.iths.redis.cache.Constant;
import com.iths.redis.cache.LockCallback;
import com.iths.redis.cache.LockHold;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * @author sen.huang
 * @date 2019/1/29.
 */
public class CacheImpl implements Cache {

    private RedisTemplate redisTemplate;

    private RedisSerializer keySerializer;

    private RedisSerializer valueSerializer;

    private static final String ACQUIRE_PREFIX = "acquire.";
    private static final String ACQUIRE_KEY_SUFFIX = ".key";
    private static final String ACQUIRE_VAL_SUFFIX = ".val";

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
                redisConnection.pSetEx(kb,expire,vb);
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
                redisConnection.pSetEx(kb,expire,vb);
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


    public void acquireLock(String lockKey, LockCallback lockCallback) {
        this.acquireLock(lockKey,lockCallback,0L);
    }

    public void acquireLock(String lockKey, LockCallback lockCallback, Long waitTime) {
        this.acquireLock(lockKey,lockCallback,waitTime,Constant.REDIS_LOCK_DEFAULT_EXPIRE_TIME);
    }

    public void acquireLock(final String lockKey, final LockCallback lockCallback, final Long waitTime, final Long lockExpireTime) {
        redisTemplate.execute(new RedisCallback() {
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String realLockKey = getRealLockKey(lockKey);
                String releaseCode = getReleaseCode();
                String lockValue = getLockValue(lockKey);
                LockHold lockHold = new LockHold(releaseCode, lockValue);
                try {
                    byte[] keyBytes = keySerializer.serialize(realLockKey);
                    //以100毫秒为循环单位
                    long unit = waitTime/100 + (waitTime%100>0?1:0);
                    do{
                        unit--;
                        byte[] oldValBytes = redisConnection.get(keyBytes);
                        if(oldValBytes == null){
                            byte[] valueBytes = valueSerializer.serialize(lockHold);
                            //持有锁设置锁超时时间——需要设置事务，以免线程1先获取到值为null，在线程1在set值之前，另一个线程也也获取到null，然后set值，这样线程内代码执行了两次。
                            try {
                                redisConnection.watch(keyBytes);
                                redisConnection.multi();
                                System.out.println("线程["+Thread.currentThread().getName()+"]到达");
                                redisConnection.pSetEx(keyBytes,lockExpireTime,valueBytes);
                                redisConnection.exec();
                                System.out.println("线程["+Thread.currentThread().getName()+"]获得锁");
                                lockCallback.process();
                                return null;
                            } catch (Exception e) {
                                //@TODO 警告级别的日志
                                System.out.println("线程["+Thread.currentThread().getName()+"]acquireLock:183,error"+e.getMessage());
                            }
                        }else{
                            LockHold oldLockHold = (LockHold)valueSerializer.deserialize(oldValBytes);
                            if(oldLockHold == null || !oldLockHold.getLockValue().equals(lockValue)){
                                throw new RuntimeException("lock.key["+lockKey+"] is lock.value.error,error data["+oldLockHold.getLockValue()+"]");
                            }
                        }
                        if(waitTime == 0L){
                            return null;
                        }
                        System.out.println("cycle");
                        Thread.sleep(100L);
                    }while (unit>0);
                } catch (Exception e) {
                    //@TODO 加上日志输出
                    throw new RuntimeException(e);
                } finally {
                    release(realLockKey,releaseCode);
                }
                return null;
            }
        });
    }

    private String getRealLockKey(String key){
        return ACQUIRE_PREFIX + key + ACQUIRE_KEY_SUFFIX;
    }

    private String getLockValue(String value){
       return ACQUIRE_PREFIX + value + ACQUIRE_VAL_SUFFIX;
    }

    private String getReleaseCode(){
        return UUID.randomUUID().toString().replace("-","");
    }
    private void release(final String realLockKey, final String releaseCode){
        redisTemplate.execute(new RedisCallback() {
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] realLockKeyBytes = keySerializer.serialize(realLockKey);
                byte[] valueBytes = redisConnection.get(realLockKeyBytes);
                if(valueBytes != null){
                    LockHold lockHold = (LockHold)valueSerializer.deserialize(valueBytes);
                    if(lockHold != null && releaseCode.equals(lockHold.getReleaseCode())){
                        try {
                            redisConnection.watch(realLockKeyBytes);
                            redisConnection.multi();
                            redisConnection.del(realLockKeyBytes);
                            redisConnection.exec();
                            System.out.println("线程["+Thread.currentThread().getName()+"]释放锁成功，code["+releaseCode+"]");
                        } catch (Exception e) {
                            //@TODO 日志输出，在做释放的时候，其他线程获得锁
                            System.out.println(e.getMessage());
                        }
                        return null;
                    }
                }
                return null;
            }
        });
    }

}
