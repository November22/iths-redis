package com.iths.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.server.LogFormatter;
import org.apache.zookeeper.server.SnapshotFormatter;
import org.junit.Test;

/**
 * @author sen.huang
 * @date 2019/3/5.
 */

public class CuratorFrameworkTest {

    @Test
    public void asyncTest() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(10,4,500);
        CuratorFramework client = CuratorFrameworkFactory.newClient("10.151.30.228:2181,10.151.31.22:2181,10.151.31.24:2181", retryPolicy);
        client.create().withMode(CreateMode.EPHEMERAL).inBackground().forPath("/path",new byte[0]);
//        client.create().inBackground().forPath("");
//        client.create().inBackground()
        CuratorListener listener = new CuratorListener(){
            public void eventReceived(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                switch (curatorEvent.getType()){
                    case DELETE:
                        //...
                }
            }
        };
        DeleteBuilder delete = client.delete();
        client.getCuratorListenable().addListener(listener);
        UnhandledErrorListener errorListener = new UnhandledErrorListener(){
            public void unhandledError(String s, Throwable throwable) {

            }
        };
        client.getUnhandledErrorListenable().addListener(errorListener);
        LeaderSelectorListener leaderSelectorListener = new LeaderSelectorListener(){
            public void takeLeadership(CuratorFramework curatorFramework) throws Exception {

            }

            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {

            }
        };

    }
}
