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

/**
 * @author sen.huang
 * @date 2019/2/26.
 */
public class Worker implements Watcher {

    private ZooKeeper zooKeeper;

    private String hosts;

    /**
     * 从节点编号
     */
    private String serverId = Integer.toString(new Random().nextInt());

    /**
     * 从节点状态
     */
    private String status;

    public void setStatus(String status){
        this.status = status;
    }

    public Worker(String hosts){
        this.hosts = hosts;
    }

    public void startZk() throws IOException {
        zooKeeper = new ZooKeeper(this.hosts,15000,this);
    }

    public void process(WatchedEvent watchedEvent) {
        System.out.println(watchedEvent.toString());
    }

    public void register(){
        zooKeeper.create("/workers/worker-"+serverId,
                "Idle".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL,
                createWorkerCallback,null);
    }

    /**
     * 更新从节点的状态
     * @param status
     */
    synchronized private void updateStatus(String status){
        if(this.status == status){
            //-1 表示不检查node的version，强制变更
            zooKeeper.setData("/workers/worker-"+serverId,status.getBytes(),-1,setStatusCallback,status);
        }
    }

    /**
     * set node 值的异步回调
     */
    AsyncCallback.StatCallback setStatusCallback = new AsyncCallback.StatCallback() {
        public void processResult(int i, String path, Object ctx, Stat stat) {
            switch (KeeperException.Code.get(i)){
                case CONNECTIONLOSS:
                    updateStatus((String)ctx);
            }
        }
    };

    /**
     * 创建从节点回调
     */
    AsyncCallback.StringCallback createWorkerCallback = new AsyncCallback.StringCallback() {
        public void processResult(int code, String path, Object ctx, String name) {
            System.out.println(KeeperException.Code.get(code));
            switch (KeeperException.Code.get(code)){
                case CONNECTIONLOSS:
                    register();
                    break;
                case OK:
                    System.out.println("注册从节点成功["+serverId+"]");
                    break;
                case NODEEXISTS:
                    System.out.println("注册从节点已存在["+serverId+"]");
                default:
                    throw new RuntimeException(""+KeeperException.Code.get(code));
            }
        }
    };



    public static void main(String[] args) throws IOException, InterruptedException {
        Worker worker = new Worker("127.0.0.1:2181");
        worker.startZk();
        worker.register();
        Thread.sleep(12000L);
    }
}
