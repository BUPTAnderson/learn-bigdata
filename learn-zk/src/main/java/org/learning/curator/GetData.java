package org.learning.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.data.Stat;
import org.learning.User;

import static org.learning.SubscribeDataChanges.byteArrayToObject;

/**
 * Created by Anderson on 2018/9/13
 * 获取节点内容
 */
public class GetData {
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

        byte[] ret = client.getData().storingStatIn(stat).forPath("/jike/aa");

        Object o = byteArrayToObject((byte[]) ret);
        System.out.println(o.getClass());
        if (o instanceof User) {
            System.out.println(((User) o).toString());
        } else {
            System.out.println(o.toString());
        }

        System.out.println(stat);
    }
}
