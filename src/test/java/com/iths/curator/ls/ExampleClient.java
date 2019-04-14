package com.iths.curator.ls;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

/**
 * LeaderSelectorListenerAdapter 实现了LeaderSelectorListener接口的stateChanged方法-帮助管理服务的连接状态
 */
public class ExampleClient extends LeaderSelectorListenerAdapter implements Closeable{

    private final String name;

    private final LeaderSelector leaderSelector;

    private final AtomicInteger leaderCount = new AtomicInteger();

    public ExampleClient(CuratorFramework client,String path, String name) {
        leaderSelector = new LeaderSelector(client, path, this);
        this.name = name;
    }

    public void start() throws IOException{
        leaderSelector.start();
    }

    @Override
    public void close() throws IOException{
        leaderSelector.close();
    }

    /**
     * 获取到领导权执行此方法，方法执行完成后，释放领导权，然后所有进程再次竞争获取
     * @param client
     * @throws Exception
     */
	public void takeLeadership(CuratorFramework client) throws Exception {
        final int waitSeconds = (int)(5 * Math.random()) + 1;
        System.out.println(name + " is now the leader. Waiting " + waitSeconds + " seconds...");
        System.out.println(name + " has been leader " + leaderCount.getAndIncrement() + " time(s) before.");
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(waitSeconds));
        } finally {
            System.out.println(name + " relinquishing leadership.\n");
            //无此下方法，则该进程释放领导权后，不参与竞争领导权，所以在所有进程执行完成一次此方法后，不再调用takeLeadership方法。
            leaderSelector.autoRequeue();
        }
	}

	public void autoRequeue(){
        leaderSelector.autoRequeue();
    }
}