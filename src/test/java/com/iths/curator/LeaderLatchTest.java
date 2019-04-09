package com.iths.curator;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 一旦启动， LeaderLatch会和其它使用相同【latch path（即znode path）】的其它LeaderLatch交涉，然后随机的选择其中一个作为leade
 * @author sen.huang
 * @date 2019/4/9.
 */
public class LeaderLatchTest {

    private static final int CLIENT_QTY = 10;

    private static final String PATH = "/latch/leader";

    public static void main(String[] args) throws Exception {
        List<CuratorFramework> clients = Lists.newArrayList();
        List<LeaderLatch> latches = Lists.newArrayList();
        for (int i = 0; i < CLIENT_QTY; ++i) {
            CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.01:2181", new ExponentialBackoffRetry(1000, 3));
            clients.add(client);
            LeaderLatch leaderLatch = new LeaderLatch(client, PATH, "Client#" + i);
            //添加一个成为领导者的监听器
            leaderLatch.addListener(new LeaderListener(leaderLatch));
            latches.add(leaderLatch);
            //为client添加状态监听器
            client.getConnectionStateListenable().addListener(new ConnectionStateListenerImpl("Client#" + i));
            //客户端启动
            client.start();
            //群首闩启动使用
            leaderLatch.start();
        }
        LeaderLatch currentLeader = null;
        int i = 0;
        while (true){
            LeaderLatch leaderLatch = latches.get(i%10);
            if (leaderLatch.hasLeadership()){
                //直到有一个进程成为leader
                currentLeader = leaderLatch;
                System.out.println("已有节点成为leader，循环次数["+i+"]");
                break;
            }
            i++;
            Thread.sleep(100L);
        }

        System.out.println("释放当前节点 " + currentLeader.getId());
        //释放当前节点
        currentLeader.close();
        //await是一个阻塞方法， 尝试获取leader地位，但是未必能上位。
        latches.get(0).await(2, TimeUnit.SECONDS);
        System.out.println("Client#0尝试了竞选ledear");
        //参与竞选的每一个leaderLatch都会记录leader的id
        System.out.println("最终结果" + latches.get(0).getLeader().getId());
    }

    /**
     * LeaderLatch的监听器，群首闩成为leader后触发
     */
    static class LeaderListener implements LeaderLatchListener{

        private LeaderLatch leaderLatch ;

        public LeaderListener(LeaderLatch leaderLatch){
            this.leaderLatch = leaderLatch;
        }

        public void isLeader() {
            System.out.println(leaderLatch.getId()+"成为leader！！！");
        }

        public void notLeader() {
            System.out.println(leaderLatch.getId()+"不是leader！！");
        }
    }

    /**
     * client状态监听
     */
    static class ConnectionStateListenerImpl implements ConnectionStateListener {
        private String clientId;
        public ConnectionStateListenerImpl(String clientId){
            this.clientId = clientId;
        }
        public void stateChanged(CuratorFramework client, ConnectionState newState) {
            System.out.println(clientId+"连接状态改变,当前状态为："+newState.name());
        }
    }
}
