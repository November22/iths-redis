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
        System.out.println("连接成功");
    }


    /**
     * 获取管理权
     * @throws InterruptedException
     */
    public void runForMaster() throws InterruptedException {
        zooKeeper.create(masterPath,serverId.getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL,masterCreateCallback,null);
    }

    /**
     * 创建管理节点回掉
     */
    private AsyncCallback.StringCallback masterCreateCallback = new AsyncCallback.StringCallback() {
        public void processResult(int i, String path, Object o, String name) {
            System.out.println("path["+path+"],name["+name+"]");
            switch (KeeperException.Code.get(i)){
                case OK://执行成功
                    isLeader = true;
                    break;
                case CONNECTIONLOSS://连接丢失
                    try {
                        checkMaster();
                        break;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                case NODEEXISTS://节点已存在
                    masterExists();
                    break;
                default:
                    isLeader = false;
            }
        }
    };


    /**
     * 校验主节点是否存在
     * @throws InterruptedException
     */
    public void checkMaster() throws InterruptedException {
        zooKeeper.getData(masterPath,false,masterCheckCallback,null);
    }
    /**
     * 校验节点是否存在取值回调
     */
    private AsyncCallback.DataCallback masterCheckCallback = new AsyncCallback.DataCallback() {
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS://连接丢失
                    try {
                        checkMaster();
                        return;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                case NONODE://节点不存在
                    try {
                        runForMaster();
                        return;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                case OK://获取数据成功
                    String dataStr = new String(data);
                    if(serverId.equals(dataStr)){
                        isLeader = true;
                    }else{
                        masterExists();
                    }
                default:
                    try {
                        checkMaster();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        }
    };

    /**
     * 校验主节点存在，设置监听器
     */
    private void masterExists(){
        zooKeeper.exists("/master",masterExistsWatcher,masterExistsCallback,null);
    }

    /**
     * 主节点验证存在的异步回调
     */
    private AsyncCallback.StatCallback masterExistsCallback = new AsyncCallback.StatCallback() {
        public void processResult(int code, String path, Object ctx, Stat stat) {
            switch (KeeperException.Code.get(code)){
                case CONNECTIONLOSS://连接丢失，重新校验
                    masterExists();
                    break;
                case OK://请求成功
                    if(stat == null){
                        //节点不存在,竞争成为主节点，中间情况下，获得主节点的服务器丢失
                        try {
                            runForMaster();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    //else 节点存在，设置监听器完成
                    break;
                default://重新校验与设置监听器
                    try {
                        checkMaster();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

            }
        }
    };
    /**
     * 设置对/master节点的监视
     */
    private Watcher masterExistsWatcher = new Watcher() {
        public void process(WatchedEvent watchedEvent) {
            if(watchedEvent.getType() == Event.EventType.NodeDeleted && "/master".equals(watchedEvent.getPath())){
                try {
                    runForMaster();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
