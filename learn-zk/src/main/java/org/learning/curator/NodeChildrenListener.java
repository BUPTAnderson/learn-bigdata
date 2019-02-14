package org.learning.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryUntilElapsed;

/**
 * Created by Anderson on 2018/9/13
 * 监听子节点
 */
public class NodeChildrenListener {
    static void watch() throws Exception {
        RetryPolicy retryPolicy = new RetryUntilElapsed(5000, 1000);

        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectString("localhost:2181")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();

        client.start();

        final PathChildrenCache cache = new PathChildrenCache(client, "/jike", true);
        cache.start();
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event)
                    throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        System.out.println("CHILD_ADDED:" + new String(event.getData().getData(), "utf-8"));
                        break;
                    case CHILD_UPDATED:
                        System.out.println("CHILD_UPDATED:" + event.getData());
                        break;
                    case CHILD_REMOVED:
                        System.out.println("CHILD_REMOVED:" + event.getData());
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public static void main(String[] args) throws Exception {
        watch();

        Thread.sleep(Integer.MAX_VALUE);
    }
}
