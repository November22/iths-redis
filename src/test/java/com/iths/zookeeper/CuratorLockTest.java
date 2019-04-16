package com.iths.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreV2;
import org.apache.curator.framework.recipes.locks.Lease;
import org.apache.curator.framework.recipes.shared.SharedCountReader;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Curator的锁菜单
 * @author sen.huang
 * @date 2019/2/16.
 */
public class CuratorLockTest {


    private static AtomicInteger executeThreads = new AtomicInteger(0);
    private static AtomicInteger timeOutThreads = new AtomicInteger(0);
    private CuratorFramework client ;
    private ExecutorService threadPool;
    /**
     * 初始化
     */
    @Before
    public void init(){
        //客户端连接重试策略，参数1:睡眠时间单位，2.重试次数 3.最大睡眠时间
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(10,4,500);
        client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy);
        client.start();

        int coreSize = Runtime.getRuntime().availableProcessors();
        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2 + 1);

    }

    /**
     * 分布式锁
     * @throws Exception
     */
    @Test
    public void testDistributeLock() throws Exception {
        CuratorLockTest curatorLockTest = new CuratorLockTest();
        long start = System.currentTimeMillis();
        final InterProcessMutex mutex = new InterProcessMutex(client, "/curator/lock");
        for(int i=0;i<50;i++){
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    //创建分布式锁, 锁空间的根节点路径为/curator/lock
                    try {
                        //acquire不传入参数就是死等到获得锁
                        //传入参数，在指定的时间范围内获得锁，就返回true，没有获得就释放锁
                        //尝试一次。立马返回，传入小于等于0的数值
                        if(mutex.acquire(-1,TimeUnit.MILLISECONDS)){
                            System.out.println(Thread.currentThread().getName()+"-start");
                            //获得了锁, 进行业务流程
                            Thread.sleep(1000L);
                            //获得资源的线程数据量
                            System.out.println(executeThreads.incrementAndGet());
                            System.out.println(Thread.currentThread().getName()+"-end");
                            //完成业务流程, 释放锁
                            mutex.release();
                        }else{
                            System.out.println("超时线程数量："+timeOutThreads.incrementAndGet());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //关闭客户端
//                    client.close();
                }
            }, "threadName[" + i + "]");
            threadPool.execute(thread);
        }
        long end = System.currentTimeMillis();
        System.out.println("execute.time["+(end-start)+"]");
        Thread.sleep(100*1000L);
    }


    /**
     * 共享读写锁
     * 对比串行的分布式锁，节约了并发执行的读锁的执行时间
     */
    @Test
    public void testInterProcessReadWriteLock() throws InterruptedException {
        InterProcessReadWriteLock readWriteLock = new InterProcessReadWriteLock(client,"/curator/rw");
        for(int i=0;i<10;i++){
            if(i%3 == 0){
                threadPool.submit(new WriteLockThread(readWriteLock.writeLock()));
            }else{
                threadPool.submit(new ReadLockThread(readWriteLock.readLock()));
            }
        }
        Thread.sleep(55 * 1000L);
    }


    /**
     * 买票系统场景，系统同时支持5个业务同时处理，后续业务等待。
     * 适用于在分布式系统中，全局下，限量的访问方式。
     */
    @Test
    public void testInterProcessSemaphoreV2() throws Exception {
        InterProcessSemaphoreV2 semaphoreV2 =
                new InterProcessSemaphoreV2(client,"/lock/semaphoreV2",5);
        for(int i=0;i<50;i++){
            threadPool.submit(new BuyTicketRunnable(semaphoreV2));
        }
        Thread.sleep(10 * 60 * 1000L);
    }


    /**
     * 买票业务线程
     */
    class BuyTicketRunnable implements Runnable{

        private InterProcessSemaphoreV2 semaphoreV2;

        public BuyTicketRunnable(InterProcessSemaphoreV2 semaphoreV2) {
            this.semaphoreV2 = semaphoreV2;
        }

        public void run() {
            Lease acquire = null;
            try {
                acquire = semaphoreV2.acquire();
                System.out.println("开始出票....");
                Thread.sleep(10*1000L);
                System.out.println("出票完成....");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if( acquire != null ) {
                    semaphoreV2.returnLease(acquire);
                }
            }
        }
    }

    /**
     * 读锁线程
     */
    class ReadLockThread implements Runnable{

        private InterProcessMutex readLock;

        public ReadLockThread(InterProcessMutex readLock) {
            this.readLock = readLock;
        }

        public void run() {
            try {
                readLock.acquire();
                System.out.println("读锁执行 start["+Thread.currentThread().getName()+"]");
                Thread.sleep(3*1000L);
                System.out.println("读锁执行 end"+Thread.currentThread().getName()+"]");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    readLock.release();
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        }
    }

    /**
     * 写锁线程
     */
    class WriteLockThread implements Runnable{

        private InterProcessMutex writeLock;


        public WriteLockThread(InterProcessMutex writeLock) {
            this.writeLock = writeLock;
        }

        public void run() {
            try {
                writeLock.acquire();
                System.out.println("写锁执行 start");
                Thread.sleep(10*1000L);
                System.out.println("写锁执行 end");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    writeLock.release();
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        }
    }

}
