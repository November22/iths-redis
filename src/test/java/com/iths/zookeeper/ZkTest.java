package com.iths.zookeeper;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author sen.huang
 * @date 2019/3/24.
 */
public class ZkTest implements Watcher {

    private ZooKeeper zooKeeper;

    public void process(WatchedEvent watchedEvent) {
        System.out.println(watchedEvent.getState());//SyncConnected
        System.out.println(watchedEvent.getType());//None
    }

    @Before
    public void init() throws IOException {
        zooKeeper = new ZooKeeper("127.0.0.1:2181",15*1000,this);
    }

    @Test
    public void testCreate() throws KeeperException, InterruptedException {
//        User user = new User();
//        String realPath = zooKeeper.create("/20190324_", "test".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
//        System.out.println(realPath);
        zooKeeper.create("/acl20190316/20190324_", "test".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, new AsyncCallback.StringCallback() {
            public void processResult(int i, String s, Object o, String s1) {
                System.out.println("rc["+i+"]");
                System.out.println("path["+s+"]");
                System.out.println("ctx["+o+"]");
                System.out.println("name["+s1+"]");
            }
        },null);
        Thread.sleep(3000L);
    }

    @Test
    public void testDelete() throws KeeperException, InterruptedException {
    }

    static class User implements Serializable{

        private static final long serialVersionUID = 5077383706772897459L;

        private String name;

        public void setName(String name){
            this.name = name;
        }

        public String getName(){
            return name;
        }
    }
}
