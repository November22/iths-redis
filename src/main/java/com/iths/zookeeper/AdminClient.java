package com.iths.zookeeper;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Date;

/**
 * @author sen.huang
 * @date 2019/2/26.
 */
public class AdminClient implements Watcher {

    private ZooKeeper zooKeeper;

    private String hosts;

    public void process(WatchedEvent watchedEvent) {

    }

    public AdminClient(String hosts){
        this.hosts = hosts;
    }

    void start() throws IOException {
        zooKeeper = new ZooKeeper(hosts,15000,this);
    }

    void listState() throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        try {
            byte[] masterData = zooKeeper.getData("/master",false,stat);
            Date createDate = new Date(stat.getCtime());
            System.out.println("masterData:["+new String(masterData)+"],since["+createDate+"]");
        } catch (KeeperException.NoNodeException e) {
            System.out.println("没有master");
        }
        System.out.println("workers...");
        for(String worker:zooKeeper.getChildren("/workers",false)){
            byte[] wData = zooKeeper.getData("/workers/"+worker,false,null);
            System.out.println("wData["+new String(wData)+"]");
        }
        System.out.println("tasks...");
        for(String task:zooKeeper.getChildren("/assgin",false)){
            System.out.println("task"+task);
        }
    }

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        AdminClient client = new AdminClient("127.0.0.1:2181");
        client.start();
        client.listState();

    }
}
