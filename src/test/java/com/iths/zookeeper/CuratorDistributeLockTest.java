package com.iths.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author sen.huang
 * @date 2019/2/16.
 */
public class CuratorDistributeLockTest implements Runnable  {


    private static AtomicInteger executeThreads = new AtomicInteger(0);
    private static AtomicInteger timeOutThreads = new AtomicInteger(0);

    @Test
    public void testLock1() throws Exception {
        CuratorDistributeLockTest curatorDistributeLockTest = new CuratorDistributeLockTest();
        int coreSize = Runtime.getRuntime().availableProcessors();
        System.out.println(coreSize);
        ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2 + 1);
        long start = System.currentTimeMillis();
        for(int i=0;i<50;i++){
            Thread thread = new Thread(curatorDistributeLockTest,"threadName["+i+"]");
            threadPool.execute(thread);
        }
        long end = System.currentTimeMillis();
        System.out.println("execute.time["+(end-start)+"]");
        Thread.sleep(100*1000L);
    }


    public void run() {
        //客户端连接重试策略，参数1:睡眠时间单位，2.重试次数 3.最大睡眠时间
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(10,4,500);
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy);
        client.start();
        //创建分布式锁, 锁空间的根节点路径为/curator/lock
        try {
            InterProcessMutex mutex = new InterProcessMutex(client, "/curator/lock");
            //acquire不传入参数就是死等到获得锁
            //传入参数，在指定的时间范围内获得锁，就返回true，没有获得就释放锁
            if(mutex.acquire(500,TimeUnit.MILLISECONDS)){
                System.out.println(Thread.currentThread().getName()+"-start");
                //获得了锁, 进行业务流程
                Thread.sleep(1000L);
                System.out.println(Thread.currentThread().getName()+"-end");
                System.out.println(executeThreads.incrementAndGet());
                //完成业务流程, 释放锁
                mutex.release();
            }else{
                System.out.println("超时["+timeOutThreads.incrementAndGet()+"]");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //关闭客户端
        client.close();
    }
}
