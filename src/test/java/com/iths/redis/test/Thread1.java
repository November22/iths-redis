package com.iths.redis.test;

/**
 * @author sen.huang
 * @date 2019/2/17.
 */
public class Thread1 implements Runnable {

    private ThreadLocal<String> threadLocal;

    public Thread1(ThreadLocal<String> threadLocal){
        this.threadLocal = threadLocal;
    }

    public void run() {
        System.out.println("线程["+Thread.currentThread().getName()+"].ThreadLocal的值["+threadLocal.get()+"]");
        threadLocal.set("1235");
    }
}
