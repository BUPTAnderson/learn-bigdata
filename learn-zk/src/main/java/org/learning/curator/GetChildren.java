package org.learning.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;

import java.util.List;

/**
 * Created by Anderson on 2018/9/13
 * 获取子节点
 */
public class GetChildren {
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

        List<String> cList = client.getChildren().forPath("/jike");
        System.out.println(cList.size());
        for (String child : cList) {
            System.out.println(child);
        }
    }
}
