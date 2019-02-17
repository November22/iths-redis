package com.iths;

/**
 * @author sen.huang
 * @date 2019/2/16.
 */
public class ThreadContext  {
    private static ThreadLocal<String> threadLocal = new ThreadLocal<String>();
    private ThreadContext(){
    }
    public static ThreadLocal<String> getThreadLocal(){
        return threadLocal;
    }
}
