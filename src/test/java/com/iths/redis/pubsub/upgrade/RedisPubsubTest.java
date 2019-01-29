package com.iths.redis.pubsub.upgrade;

import com.iths.redis.pubsub.MessageHandleSubscriber;
import com.iths.redis.pubsub.MessagePublisher;
import com.iths.redis.pubsub.UserInfo;
import com.iths.redis.pubsub.MessageHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;

/**
 * 对于方法testPub1和testPub2，同时运行，其实是两个Spring容器环境，可看作生产的两个应用节点
 * 对于redis的pub两个容器都能监听到，
 * 但是两个容器中的handleMap存储的处理器不通，所以容器只能处理自己监听的处理器
 * @author sen.huang
 * @date 2019/1/21.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-redis.xml"})
public class RedisPubsubTest  {

    @Test
    public void testPub1() throws Exception{
        MessageHandleSubscriber.subscribe("testPub1", new MessageHandler() {
            public void handle(Object value) {
                HashMap<String,Object> map = (HashMap<String,Object>)value;
                UserInfo userInfo = (UserInfo)map.get("useInfo");
                System.out.println("testPub1.userInfo.name["+userInfo.getName()+"],age["+userInfo.getAge()+"]");
            }
        });

        HashMap<String,Object> map = new HashMap<String,Object>();
        UserInfo userInfo = new UserInfo("三棵树",25);
        map.put("useInfo",userInfo);
        MessagePublisher.publish("testPub1",map);
        Thread.sleep(30000L);
    }

    @Test
    public void testPub2() throws Exception{
        MessageHandleSubscriber.subscribe("testPub2", new MessageHandler() {
            public void handle(Object value) {
                HashMap<String,Object> map = (HashMap<String,Object>)value;
                UserInfo userInfo = (UserInfo)map.get("useInfo");
                System.out.println("testPub2.userInfo.name["+userInfo.getName()+"],age["+userInfo.getAge()+"]");
            }
        });

        HashMap<String,Object> map = new HashMap<String,Object>();
        UserInfo userInfo = new UserInfo("四棵树",26);
        map.put("useInfo",userInfo);
        MessagePublisher.publish("testPub2",map);
        Thread.sleep(30000L);
    }
}
