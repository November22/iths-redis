package com.iths;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sen.huang
 * @date 2019/2/16.
 */
public class ThreadContext  {
    private static ThreadLocal<Map<String,Object>> threadLocal;

    static {
        threadLocal = new ThreadLocal<Map<String,Object>>();
    }

    private ThreadContext(){}

    public static void set(String key,Object object){
        if(threadLocal.get() == null){
            Map<String,Object> threadContext = new HashMap<String, Object>();
            threadLocal.set(threadContext);
        }
        threadLocal.get().put(key,object);
    }

    public static <T> T get(String key){
        return threadLocal.get() == null? null:(T)threadLocal.get().get(key);
    }

    public static void remove(String key){
        if(threadLocal.get() != null){
            threadLocal.get().remove(key);
        }
    }

    public static void clear(){
        if(threadLocal.get() != null){
            threadLocal.get().clear();
        }
    }

}
