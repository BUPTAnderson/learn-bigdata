package org.learning.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;

/**
 * Created by Anderson on 2018/9/13
 * 节点删除
 */
public class DelNode {
    public static void main(String[] args) throws Exception {
        //RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        //RetryPolicy retryPolicy = new RetryNTimes(5, 1000);
        RetryPolicy retryPolicy = new RetryUntilElapsed(5000, 1000);
//        CuratorFramework client = CuratorFrameworkFactory
//                .newClient("192.168.1.105:2181",5000,5000, retryPolicy);

        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectString("localhost:2181")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();

        client.start();

        client.delete().guaranteed().deletingChildrenIfNeeded().withVersion(-1).forPath("/jike/aa");

        Thread.sleep(Integer.MAX_VALUE);
    }
}
