package com.iths.redis.base;

import redis.clients.jedis.JedisPubSub;

/**
 * redis发布订阅消息监听器
 * @author sen.huang
 * @date 2019/1/20.
 */
public class RedisMsgPubSubListener extends JedisPubSub {
    @Override
    public void unsubscribe() {
        super.unsubscribe();
    }

    @Override
    public void unsubscribe(String... channels) {
        super.unsubscribe(channels);
    }

    @Override
    public void subscribe(String... channels) {
        super.subscribe(channels);
    }

    @Override
    public void psubscribe(String... patterns) {
        super.psubscribe(patterns);
    }

    @Override
    public void punsubscribe() {
        super.punsubscribe();
    }

    @Override
    public void punsubscribe(String... patterns) {
        super.punsubscribe(patterns);
    }

    /**
     * 监听到订阅频道接受到消息时的回调
     * @param channel
     * @param message
     */
    @Override
    public void onMessage(String channel, String message) {
        System.out.println("onMessage: channel[{"+channel+"}], message[{"+message+"}]");
    }

    /**
     * 监听到订阅模式接受到消息时的回调
     * @param pattern
     * @param channel
     * @param message
     */
    @Override
    public void onPMessage(String pattern, String channel, String message) {
        System.out.println("onPMessage: pattern[{"+pattern+"}], channel[{"+channel+"}], message[{"+message+"}]");
    }

    /**
     * 订阅频道时的回调
     * @param channel
     * @param subscribedChannels
     */
    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        System.out.println("onSubscribe: channel[{"+channel+"}], subscribedChannels[{"+subscribedChannels+"}]");
    }

    /**
     * 取消订阅模式时的回调
     * @param pattern
     * @param subscribedChannels
     */
    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        System.out.println("onPUnsubscribe: pattern[{"+pattern+"}], subscribedChannels[{"+subscribedChannels+"}]");
    }

    /**
     * 订阅频道模式时的回调
     * @param pattern
     * @param subscribedChannels
     */
    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        System.out.println("onPSubscribe: pattern[{"+pattern+"}], subscribedChannels[{"+subscribedChannels+"}]");
    }

    /**
     *
     * @param channel
     * @param subscribedChannels
     */
    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        System.out.println("channel:{"+channel+"} is been subscribed:{"+subscribedChannels+"}");
    }
}
