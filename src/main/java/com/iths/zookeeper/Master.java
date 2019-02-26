package com.iths.zookeeper;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * @author sen.huang
 * @date 2019/2/26.
 */
public class Master implements Watcher {

    private static ZooKeeper zooKeeper;

    private String hosts;

    private static String serverId = Long.toString(new Random().nextLong());

    private static boolean isLeader = false;

    private static String masterPath = "/master";

    private CountDownLatch countDownLatch;

    /**
     * 一个对象的构造函数没有完成前不要调用这个对象的其他方法
     */
    public Master(String hosts){
//        countDownLatch = new CountDownLatch(1);
        this.hosts = hosts;
    }

    /**
     * 初始元数据
     */
    public void bootstrap(){
        createParent("/workers",new byte[0]);
        createParent("/tasks",new byte[0]);
        createParent("/assign",new byte[0]);
    }

    private void createParent(String path,byte[] data){
        zooKeeper.create(path,data,ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL,createParentCallback,data);
    }

    /**
     * 初始化元节点异步回掉
     */
    AsyncCallback.StringCallback createParentCallback = new AsyncCallback.StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    createParent(path,(byte[])ctx);
                case OK:
                    System.out.println("parent create");
                    break;
                case NODEEXISTS:
                    System.out.println("parent exists");
                    break;
                default:
                    throw new RuntimeException(KeeperException.create(KeeperException.Code.get(rc),path));
            }
        }
    };


    public void process(WatchedEvent event) {
        if(event.getState() == Watcher.Event.KeeperState.SyncConnected){
            countDownLatch.countDown();
        }
    }

    public void startZk() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(this.hosts,15000,this);
//        countDownLatch.wait();
        System.out.println("连接成功");
    }

    public void checkMaster() throws InterruptedException {
//        while (true){
//            Stat stat = new Stat();
//            try {
//                byte[] data = zooKeeper.getData(masterPath, false, stat);
//                isLeader = new String(data).equals(serverId);
//                return true;
//            } catch (KeeperException.NoNodeException e ){
//                return false;
//            }catch (KeeperException e) {}
//        }
        zooKeeper.getData(masterPath,false,masterCheckCallback,null);
    }

    public void runForMaster() throws InterruptedException {
//        while (true){
//            try {
//                zooKeeper.create(masterPath, serverId.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
//                isLeader = true;
//                break;
//            } catch (KeeperException.NodeExistsException e) {
//                isLeader = false;
//                break;
//            } catch (KeeperException e){}
//            if (checkMaster()) break;
//        }
        zooKeeper.create(masterPath,serverId.getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL,masterCreateCallback,null);
    }

    AsyncCallback.DataCallback masterCheckCallback = new AsyncCallback.DataCallback() {
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    try {
                        checkMaster();
                        return;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                case NONODE:
                    try {
                        runForMaster();
                        return;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

            }
        }
    };

    AsyncCallback.StringCallback masterCreateCallback = new AsyncCallback.StringCallback() {
        public void processResult(int i, String path, Object o, String name) {
            System.out.println("path["+path+"],name["+name+"]");
            switch (KeeperException.Code.get(i)){
                case OK:
                    isLeader = true;
                    break;
                case CONNECTIONLOSS:
                    try {
                        checkMaster();
                        break;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                default:
                    isLeader = false;
            }
        }
    };

    public static void main(String[] args) throws IOException, InterruptedException {
        Master master = new Master("127.0.0.1:2181");
        master.startZk();
        master.runForMaster();
        master.bootstrap();
        System.out.println("开始睡眠");
        Thread.sleep(1000L);
        if(isLeader){
            System.out.println("i'm the leader");
//            Thread.sleep(6000L);
        }else{
            System.out.println("other is leader");
        }
    }

}
