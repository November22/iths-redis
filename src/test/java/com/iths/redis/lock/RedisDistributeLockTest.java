package com.iths.redis.lock;

import com.iths.ThreadContext;
import com.iths.redis.cache.Cache;
import com.iths.redis.cache.LockCallback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.jedis.JedisConverters;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 *
 * @author sen.huang
 * @date 2019/1/30.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-redis.xml"})
public class RedisDistributeLockTest {

    @Autowired
    private Cache cache;

    @Autowired
    private RedisTemplate redisTemplate;

    ThreadLocal threadLocal = new ThreadLocal();

    @Test
    public void testLock() throws InterruptedException {
        int i = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(2 * i + 1);
        for(int j=0;j<50;j++){
            ThreadContext.set("tag",i);
            service.execute(new LockTest(""+j+"","lock.test",3000L,60*1000L));
        }
        Thread.sleep(10*60*1000L);
    }

    @Test
    public void testSet(){
        redisTemplate.execute(new RedisCallback() {
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                Jedis jedis = (Jedis) redisConnection.getNativeConnection();
                //单位毫秒
                Expiration expiration = Expiration.from(30*1000,TimeUnit.MILLISECONDS);
                byte[] expx = JedisConverters.toSetCommandExPxArgument(expiration);
                byte[] nxxx = JedisConverters.toSetCommandNxXxArgument(RedisStringCommands.SetOption.SET_IF_ABSENT);

                //得到锁
                String reply = jedis.set(redisTemplate.getKeySerializer().serialize("test001"),
                                         redisTemplate.getValueSerializer().serialize("9987"),
                                         nxxx,
                                         expx,
                                         expiration.getExpirationTime());

                System.out.println("jedis.set 响应值["+reply+"]");
                return null;
            }
        });
    }

    @Test
    public void testSetEx(){
        redisTemplate.execute(new RedisCallback() {
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                Jedis jedis = (Jedis) redisConnection.getNativeConnection();
                //单位毫秒
                Expiration expiration = Expiration.from(30*1000,TimeUnit.MILLISECONDS);
                byte[] expx = JedisConverters.toSetCommandExPxArgument(expiration);
                byte[] nxxx = JedisConverters.toSetCommandNxXxArgument(RedisStringCommands.SetOption.SET_IF_ABSENT);

                //得到锁
                Long reply = jedis.setnx("test001","test002");

                System.out.println("jedis.set 响应值["+reply+"]");
                return null;
            }
        });
    }

    @Test
    public void getLock() throws Exception{
        String lockKey = "test.lock.key";
        Thread lockThread1 = new Thread(new LockTest("",lockKey,0L,5000L));
        lockThread1.setName("LockThread1");

        Thread lockThread2 = new Thread(new LockTest("",lockKey,6000L,50*60*1000L));
        lockThread2.setName("LockThread2");

        lockThread1.start();
        Thread.sleep(500L);
        lockThread2.start();

        Thread.sleep(10*60*1000L);
    }

    class LockTest implements Runnable{
        private String tag;
        private String lockKey;
        private Long lockWaitTime;
        private Long lockExpireTime;

        public LockTest(String tag,String lockKey,Long lockWaitTime, Long lockExpireTime) {
            this.tag = tag;
            this.lockKey = lockKey;
            this.lockWaitTime = lockWaitTime;
            this.lockExpireTime = lockExpireTime;
        }

        public void run() {
//            System.out.println(ThreadContext.get("tag"));
            cache.acquireLock(lockKey, new LockCallback() {
                public void process() {
                    try {
                        long start = System.currentTimeMillis();
                        System.out.println("线程["+tag+"]开始睡眠");
                        Thread.sleep(2000L);
                        long end = System.currentTimeMillis();
                        System.out.println("线程["+tag+"]睡眠结束,睡眠时间["+(end-start)+"]");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            },lockWaitTime,lockExpireTime);
        }
    }
}
