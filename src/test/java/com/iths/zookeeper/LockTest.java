package com.iths.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author sen.huang
 * @date 2019/2/16.
 */
public class LockTest implements Runnable {

    @Test
    public void testLock1() throws Exception {
        LockTest lockTest = new LockTest();
        int coreSize = Runtime.getRuntime().availableProcessors();
        System.out.println(coreSize);
        ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2 + 1);
        long start = System.currentTimeMillis();
        for(int i=0;i<50;i++){
            Thread thread = new Thread(lockTest,"threadName["+i+"]");
            threadPool.execute(thread);
        }
        long end = System.currentTimeMillis();
        System.out.println("execute.time["+(end-start)+"]");
        Thread.sleep(100*1000L);
    }


    public void run() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,4);
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy);
        client.start();
        //创建分布式锁, 锁空间的根节点路径为/curator/lock
        try {
            InterProcessMutex mutex = new InterProcessMutex(client, "/curator/lock");
            mutex.acquire();
            System.out.println(Thread.currentThread().getName()+"-start");
            //获得了锁, 进行业务流程
            Thread.sleep(1000L);
            System.out.println(Thread.currentThread().getName()+"-end");
            //完成业务流程, 释放锁
            mutex.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //关闭客户端
        client.close();
    }
}
