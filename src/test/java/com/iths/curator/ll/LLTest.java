package com.iths.curator.ll;

import org.apache.curator.framework.recipes.leader.LeaderLatchListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 服务端竞争测试
 * @author sen.huang
 * @date 2019/4/9.
 */
public class LLTest {
    public static void main(String[] args) throws InterruptedException {

        ExecutorService executorService = new ThreadPoolExecutor(3,6,15,TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>(15));
        for(int serverIndex=0;serverIndex<5;serverIndex++){
            String serverId = "server"+serverIndex;
            executorService.submit(new ServiceProcess(serverId,new LeaderLatchListenerImpl(serverId)));
        }
        Thread.sleep(3000L);
    }

    static class LeaderLatchListenerImpl implements LeaderLatchListener {
        /**
         * 服务器的id
         */
        private String serverId;

        /**
         * 其实可以将实例化的ServiceProcess放入监听器
         * @param serverId
         */
        public LeaderLatchListenerImpl(String serverId){
            this.serverId = serverId;
        }

        public void isLeader() {
            //业务通知 serverId成为leader
            System.out.println(serverId+"成为leader！！！");
        }

        public void notLeader() {

        }
    }
}
