package com.iths.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * @author sen.huang
 * @date 2019/2/26.
 */
public class Client implements Watcher {

    private ZooKeeper zooKeeper;

    private String hosts;

    public void process(WatchedEvent watchedEvent) {}

    public Client(String hosts){
        this.hosts = hosts;
    }

    public void startZk() throws IOException {
        zooKeeper = new ZooKeeper(this.hosts,15000,this);
    }

    public String queueCommand(String command) throws KeeperException, InterruptedException {
        while (true){
            try {
                String name = zooKeeper.create("/tasks/task-",
                        command.getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT_SEQUENTIAL);
                return name;
            } catch (KeeperException.NodeExistsException e) {
                throw new RuntimeException(e);
            } catch (KeeperException.ConnectionLossException e) {
            }
        }
    }

    public static void main(String[] args) throws KeeperException, InterruptedException, IOException {
        Client client = new Client("127.0.0.1");
        client.startZk();
        client.queueCommand("cmd");

    }


}
