package com.iths.redis.pubsub;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义sub方接收到消息后，使用的线程池，主要可以自己设置线程前缀，方便查询
 * @author sen.huang
 * @date 2019/1/27.
 */
public class ListenerTaskExecutor extends ThreadPoolTaskScheduler {

    public ListenerTaskExecutor() {
        int threadPoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;
        super.setPoolSize(threadPoolSize);
        super.setThreadFactory(new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            private final String namePrefix = "redisListenerTaskExecutor-";

            public Thread newThread(Runnable r) {
                return new Thread(r, namePrefix + threadNumber.getAndIncrement());
            }
        });
    }
}
