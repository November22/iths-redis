package com.iths.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author sen.huang
 * @date 2019/2/16.
 */
public class ZnodeTest implements Watcher  {

    private ZooKeeper zooKeeper;

    public ZnodeTest(){
        System.out.println("示例话");
    }

    private CountDownLatch countDownLatch = new CountDownLatch(1);//同步计数器

    public void process(WatchedEvent watchedEvent) {
        System.out.println("event.state["+watchedEvent.getState()+"]");
        //连接事件
        if(watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected){
            countDownLatch.countDown();//计数器减一
        }
    }

    @Before
    public void init() throws IOException, InterruptedException {
        System.out.println("创建对象");
        //这个创建事件是异步的
        zooKeeper = new ZooKeeper("127.0.0.1:2181",30000,this);
        //需要计数器减为0才会执行
        countDownLatch.await();
    }

    @Test
    public void testZnode() throws KeeperException, InterruptedException {
        for(int i=0;i<50;i++){
            Thread.sleep(1*1000L);
            //参数3和4分别是权限控制（ACL），节点类型（持久，有序持久，临时，有序临时）
            String result = zooKeeper.create("/lock", ("data" + i).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println("createResult["+result+"]");
        }


        zooKeeper.close();
        System.out.println("客户端关闭");
        Thread.sleep(10*60*1000);
    }
}
