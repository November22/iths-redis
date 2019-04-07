package com.iths.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.Before;
import org.junit.Test;

/**
 * @author sen.huang
 * @date 2019/4/7.
 */
public class CuratorApiTest {
    CuratorFramework client ;

    @Before
    public void init(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(10,4,500);
        client = CuratorFrameworkFactory.newClient("127.0.01:2181", retryPolicy);
        client.start();
    }


    @Test
    public void testSyncCreate() throws Exception {
        String s = client.create().withMode(CreateMode.EPHEMERAL).forPath("/curatorTest", "233".getBytes());
        System.out.println(s);
    }

    @Test
    public void testAsyncCreate() throws Exception {
        client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).inBackground(new BackgroundCallback() {
            public void processResult(CuratorFramework zclient, CuratorEvent event) throws Exception {
                System.out.println(event.getType());
                System.out.println("event.getPath:" + event.getName());
                byte[] bytes = zclient.getData().forPath(event.getName());
//                zclient.setData().forPath(event.getName(),"6333".getBytes());
                System.out.println(new String(bytes));
                System.out.println("修改值完成");

            }
        }).forPath("/curatorTest", "233".getBytes());
        Thread.sleep(60000L);
    }

    @Test
    public void testCommonListener() throws Exception {
        CuratorListener curatorListener = new CuratorListener() {
            public void eventReceived(CuratorFramework zclient, CuratorEvent event) throws Exception {
                //对于监听器，事件类型一直是 WATCHED
                System.out.println(event.getType());
                System.out.println(event.getPath());
                byte[] bytes = zclient.getData().forPath(event.getPath());
                String string = new String(bytes);
                System.out.println(string);
            }
        };
        client.getData().watched().inBackground().forPath("/assgin");
        client.getCuratorListenable().addListener(curatorListener);
        Thread.sleep(60000L);
    }

    @Test
    public void testErrorListener(){
        UnhandledErrorListener errorListener = new UnhandledErrorListener() {
            public void unhandledError(String message, Throwable e) {
                //异常处理
            }
        };
        client.getUnhandledErrorListenable().addListener(errorListener);
    }
}
