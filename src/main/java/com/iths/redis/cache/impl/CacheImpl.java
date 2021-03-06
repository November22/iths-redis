package com.iths.redis.cache.impl;

import com.iths.ThreadContext;
import com.iths.redis.cache.Cache;
import com.iths.redis.cache.Constant;
import com.iths.redis.cache.LockCallback;
import com.iths.redis.cache.LockHold;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.jedis.JedisConverters;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    public boolean acquire(final String lockKey,final Long waitTime,final Long lockExpireTime) {
        return (Boolean) redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String realLockKey = getRealLockKey(lockKey);
                String releaseCode = getReleaseCode();
                //透传
                ThreadContext.set(releaseCode);
                Long timeoutCount = waitTime;
                while (timeoutCount > 0) {
                    byte[] keyByte = keySerializer.serialize(realLockKey);
                    Jedis jedis = (Jedis) redisConnection.getNativeConnection();
                    Expiration expiration = Expiration.from(lockExpireTime,TimeUnit.MILLISECONDS);
                    byte[] expx = JedisConverters.toSetCommandExPxArgument(expiration);
                    byte[] nxxx = JedisConverters.toSetCommandNxXxArgument(RedisStringCommands.SetOption.SET_IF_ABSENT);
                    //锁——原子性的
                    //参数说明：1.用来当锁的key，2.key对应的value 3.set的操作方式（SET_IF_ABSENT，不存在的时候操作）  4.过期时间
                    String reply = jedis.set(keyByte,keySerializer.serialize(releaseCode),nxxx,expx,
                            expiration.getExpirationTime());
                    if (reply != null) {
                        System.out.println(releaseCode);
                        return true;
                    }
                    timeoutCount -= 100;
                    if (timeoutCount <= 0){
//                        System.out.println(Thread.currentThread().getName()+"1锁超时");
                        //锁等超时
                        return false;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
//                System.out.println(Thread.currentThread().getName()+"2锁超时");
                return false;
            }
        });
    }

    public void acquireLock(String lockKey, LockCallback lockCallback) {
        this.acquireLock(lockKey,lockCallback,0L);
    }

    public void acquireLock(String lockKey, LockCallback lockCallback, Long waitTime) {
        this.acquireLock(lockKey,lockCallback,waitTime,Constant.REDIS_LOCK_DEFAULT_EXPIRE_TIME);
    }

//    public void acquireLock(final String lockKey, final LockCallback lockCallback, final Long waitTime, final Long lockExpireTime) {
//        redisTemplate.execute(new RedisCallback() {
//            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
//                String realLockKey = getRealLockKey(lockKey);
//                String releaseCode = getReleaseCode();
//                String lockValue = getLockValue(lockKey);
//                LockHold lockHold = new LockHold(releaseCode, lockValue);
//                try {
//                    byte[] keyBytes = keySerializer.serialize(realLockKey);
//                    //以100毫秒为循环单位
//                    long unit = waitTime/100 + (waitTime%100>0?1:0);
//                    do{
//                        unit--;
//                        byte[] oldValBytes = redisConnection.get(keyBytes);
//                        if(oldValBytes == null){
//                            byte[] valueBytes = valueSerializer.serialize(lockHold);
//                            //持有锁设置锁超时时间——需要设置事务，以免线程1先获取到值为null，在线程1在set值之前，另一个线程也也获取到null，然后set值，这样线程内代码执行了两次。
//                            try {
////                                @TODO 不能满足多线程要求，如果两个线程，同时进入这个try-catch，在执行watch(keyBytes)前失去时间片，另一个线程执行完成exec后，停止的线程获得时间片
////                                redisConnection.watch(keyBytes);
////                                redisConnection.multi();
////                                redisConnection.pSetEx(keyBytes,lockExpireTime,valueBytes);
////                                redisConnection.exec();
//
//                                //方式2
//                                System.out.println("线程["+ThreadContext.getThreadLocal().get()+"].expire["+lockExpireTime+"]");
//                                lockCallback.process();
//                                return null;
//                            } catch (Exception e) {
//                                //@TODO 警告级别的日志
//                                System.out.println("锁竞争异常");
//                            }
//                        }else{
//                            LockHold oldLockHold = (LockHold)valueSerializer.deserialize(oldValBytes);
//                            if(oldLockHold == null || !oldLockHold.getLockValue().equals(lockValue)){
//                                throw new RuntimeException("lock.key["+lockKey+"] is lock.value.error,error data["+oldLockHold.getLockValue()+"]");
//                            }
//                        }
//                        if(waitTime == 0L){
//                            return null;
//                        }
//                        Thread.sleep(100L);
//                    }while (unit>0);
//                    System.out.println("线程["+ThreadContext.getThreadLocal().get()+"]锁等超时");
//                } catch (Exception e) {
//                    //@TODO 加上日志输出
//                } finally {
//                    release(realLockKey,releaseCode);
//                }
//                return null;
//            }
//        });
//    }


    public void acquireLock(final String lockKey,final LockCallback lockCallback,final Long waitTime,final Long lockExpireTime) {
        redisTemplate.execute(new RedisCallback() {
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                if(acquire(lockKey,waitTime,lockExpireTime)){
                    try {
                        lockCallback.process();
                    } finally {
                        String realLockKey = getRealLockKey(lockKey);
                        String releaseCode  = ThreadContext.get(realLockKey);
                        release(realLockKey,releaseCode);
                    }
                }
                return null;
            }
        });
    }

    private String getRealLockKey(String key){
        return ACQUIRE_PREFIX + key + ACQUIRE_KEY_SUFFIX;
    }

    private String getReleaseCode(){
        return UUID.randomUUID().toString().replace("-","");
    }
    private void release(final String realLockKey, final String releaseCode){
        redisTemplate.execute(new RedisCallback() {
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                Jedis jedis = (Jedis)redisConnection.getNativeConnection();
                long result = (Long)jedis.eval(script, Collections.singletonList(realLockKey), Collections.singletonList(releaseCode));
                if (1l == (result)) {
                    System.out.println(releaseCode);
                    ThreadContext.remove(realLockKey);
                }
                return null;
            }
        });
    }


   protected static class ThreadContext  {
        //不同的ThreadLocal对同一个线程，管理不同的对象信息
       //因为对ThreadLocal.ThreadLocalMap threadLocals = null; 存储的key是threadLocal实例
        private static ThreadLocal<String> threadLocal;

        static {
            threadLocal = new ThreadLocal<String>();
        }

        private ThreadContext(){}

        public static void set(String releaseCode){
            threadLocal.set(releaseCode);
        }

        public static String get(String key){
            return threadLocal.get();
        }

        public static void remove(String key){
            threadLocal.remove();
        }
    }

}
