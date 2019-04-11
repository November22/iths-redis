package com.iths.curator.ls;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class LeaderSelectorTest {
    public static void main(String[] args) throws InterruptedException {
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.01:2181",
                new ExponentialBackoffRetry(1000, 3));
        LeaderSelector leaderSelector = new LeaderSelector(client,"/master",new LeaderSelectorListenerImpl());
        client.start();
        leaderSelector.autoRequeue();  
        leaderSelector.start();
        System.out.println(client);
        // leaderSelector.close();
        Thread.sleep(30000L);
    }

    static class LeaderSelectorListenerImpl implements LeaderSelectorListener {

        public void stateChanged(CuratorFramework client, ConnectionState newState) {
            switch(newState){
                case CONNECTED:
                    break;
                case RECONNECTED:
                    break;
                case SUSPENDED:
                    break;
                case LOST:
                    break;
                case READ_ONLY:
                    break;
                default:
                    System.out.println("不存在的状态。。。");
            }
        }

        /**
         * 取得领导权，方法被调用
         */
        @Override
        public void takeLeadership(CuratorFramework client) throws Exception {
           System.out.println("领导！！！");
        }
    }
}