package com.iths.redis.test;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author sen.huang
 * @date 2019/2/17.
 */
public class ThreadLocalTest implements Runnable{

    private ThreadLocal<String> threadLocal = new ThreadLocal<String>();
    int i = Runtime.getRuntime().availableProcessors();
    ExecutorService executorService = Executors.newFixedThreadPool( 2);

    /**
     * new Thread(new Runnable() {
     *                 public void run() {
     *                     System.out.println("线程["+Thread.currentThread().getName()+"].ThreadLocal的值["+threadLocal.get()+"]");
     *                     threadLocal.set("1235");
     *                 }
     *             })//会线程池中线程遗留ThreadLocal中的信息
     *             //在线程内部方法调用线程池，然后线程对象为当前线程？？
     * @throws InterruptedException
     */

    @Test
    public void testThreadLocal() throws InterruptedException {

        for(int j=0;j<50;j++){
            executorService.execute(new Thread1(threadLocal));
        }
        Thread.sleep(3000L);
    }

    public void run() {
        System.out.println("线程["+Thread.currentThread().getName()+"].ThreadLocal的值["+threadLocal.get()+"]");
        threadLocal.set("1235");
    }

    @Test
    public void test2() {
        for (int i = 1; i < 4; i++) {
            final int count = i;
            executorService.submit(new Runnable() {
                public void run() {
                    System.out.println("第" + count + "次循环刚开始，ThreadLocal中的值为：" + threadLocal.get());
                    threadLocal.set(count + "---");
                    System.out.println("第" + count + "次循环结束，ThreadLocal中的值为：" + threadLocal.get());
                    System.out.println("当前线程名称为：" + Thread.currentThread().getName());
                    System.out.println("------------------");
                }
            });
        }
    }
}
