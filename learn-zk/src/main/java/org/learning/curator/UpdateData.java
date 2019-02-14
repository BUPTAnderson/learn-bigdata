package org.learning.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.data.Stat;

/**
 * Created by Anderson on 2018/9/13
 * 节点修改
 */
public class UpdateData {
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

        Stat stat = new Stat();
        client.getData().storingStatIn(stat).forPath("/jike");
        System.out.println(stat.getVersion());
        client.setData().withVersion(stat.getVersion()).forPath("/jike", "1234".getBytes());
    }
}
