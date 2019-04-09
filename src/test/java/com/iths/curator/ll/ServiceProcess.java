package com.iths.curator.ll;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * 服务进程：模拟服务端的一个服务进程
 * @author sen.huang
 * @date 2019/4/9.
 */
public class ServiceProcess implements Runnable{

    /**
     * 竞争路径
     */
    private static final String LEADER_PATH = "/master";

    /**
     * 群首闩监听器
     * 成为leader的进程触发后，进行通知其他业务，次服务端成为leader。
     */
    private LeaderLatchListener leaderLatchListener;

    /**
     * 服务器的id
     */
    private String serverId;

    /**
     * @TODO 有需要可以传入CuratorFramework状态监听器，即ConnectionStateListener的实现，对服务端的连接状态进行监控
     * client.getConnectionStateListenable().addListener
     * @param serverId 分配服务id
     * @param leaderLatchListener 统一的监听器
     */
    public ServiceProcess(String serverId,LeaderLatchListener leaderLatchListener){
        this.serverId = serverId;
        this.leaderLatchListener = leaderLatchListener;
    }

    public void run() {
        //连接Zookeeper服务端
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.01:2181", new ExponentialBackoffRetry(1000, 3));
        //创建群首闩
        LeaderLatch leaderLatch = new LeaderLatch(client,LEADER_PATH,serverId);
        //添加群首监听
        leaderLatch.addListener(leaderLatchListener);
        client.start();
        try {
            leaderLatch.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ......其他业务的方法
     */

}
