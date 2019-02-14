package org.learning.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.RetryUntilElapsed;

/**
 * Created by Anderson on 2018/9/13
 * 节点监听
 */
public class NodeListener {
    public static void main(String[] args) throws Exception {
        RetryPolicy retryPolicy = new RetryUntilElapsed(5000, 1000);

        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectString("localhost:2181")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();

        client.start();

        final NodeCache cache = new NodeCache(client, "/jike");
        cache.start();
        cache.getListenable().addListener(new NodeCacheListener() {
            public void nodeChanged() throws Exception {
                byte[] ret = cache.getCurrentData().getData();
                System.out.println("new data:" + new String(ret));
            }
        });

        Thread.sleep(Integer.MAX_VALUE);
    }
}
