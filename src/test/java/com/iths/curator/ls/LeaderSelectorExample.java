package com.iths.curator.ls;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;


public class LeaderSelectorExample {
    private static final int CLIENT_QTY = 10;

    private static final String PATH = "/examples/leader";

    public static void main(String[] args) throws IOException {
        List<CuratorFramework> clients = Lists.newArrayList();
        List<ExampleClient> examples = Lists.newArrayList();

        for (int i = 0; i < CLIENT_QTY; ++i) {
            CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.01:2181", new ExponentialBackoffRetry(1000, 3));
            clients.add(client);

            ExampleClient example = new ExampleClient(client, PATH, "Client #" + i);
            examples.add(example);

            client.start();
            example.start();
        }

    }
}