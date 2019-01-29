package com.iths.redis.test;

import com.iths.redis.cache.Cache;
import com.iths.redis.pubsub.UserInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author sen.huang
 * @date 2019/1/29.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-redis.xml"})
public class CacheTest  {
    @Autowired
    private Cache cache;

    /**
     * put Object
     */
    @Test
    public void testPut(){
        cache.put("iths.redis.001",new UserInfo("三棵树",23));
    }

    @Test
    public void testPutExpire(){
        cache.put("iths.redis.002",new UserInfo("三棵树",53),120);
    }

    @Test
    public void testPutLock(){
        cache.put("iths.redis.003",new UserInfo("三棵树",24), 120);
    }

    @Test
    public void persist(){
        cache.persist("iths.redis.004",new UserInfo("三棵树",25));
    }

    @Test
    public void testGet(){
        UserInfo info = cache.get("iths.redis.004");
        System.out.println(info);
    }

    @Test
    public void testDel(){
        cache.remove("iths.redis.004");
    }

    /**
     * 测试事务testTx1+testTx2
     * 生效
     */
    @Test
    public void testTx1(){
        cache.putLock("iths.redis.0049",new UserInfo("三棵树",25));
    }

    @Test
    public void testTx2(){
        cache.put("iths.redis.004",new UserInfo("三棵树",29));
    }

}
