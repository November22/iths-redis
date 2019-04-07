package com.iths.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;

/**
 * @author sen.huang
 * @date 2019/3/16.
 */
public class AclTest implements Watcher {

    private ZooKeeper zooKeeper;

    private CountDownLatch countDownLatch = new CountDownLatch(1);//同步计数器

    public void process(WatchedEvent watchedEvent) {
        System.out.println("响应事件类型："+watchedEvent.getState());
        //连接事件
        if(watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected){
            countDownLatch.countDown();//计数器减一
        }
    }

    @Before
    public void init() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper("127.0.0.1:2181",30000,this);
        countDownLatch.await();
    }

    @Test
    public void addAcl() throws KeeperException, InterruptedException {
//        zooKeeper.addAuthInfo("auth","zkAclTest:9527".getBytes());
        zooKeeper.create("/acl20190316","233".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);

    }

    @Test
    public void testDigestAuthenticationProvider() throws NoSuchAlgorithmException {
        String digest = DigestAuthenticationProvider.generateDigest("super:9527");
        System.out.println(digest);
    }


}
