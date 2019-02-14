package org.learning;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import java.util.List;

/**
 * Created by Anderson on 2018/9/13
 * 订阅子节点列表变化
 */
public class SubscribeChildChanges {
    private static class ZkChildListener implements IZkChildListener {
        public void handleChildChange(String parentPath,
                                      List<String> currentChilds) throws Exception {
            System.out.println(parentPath);
            System.out.println(currentChilds.toString());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ZkClient zc = new ZkClient("localhost:2181", 10000, 10000, new SerializableSerializer());
        System.out.println("conneted ok!");

        // 除子节点变化外，节点本身创建和删除也会收到通知
        zc.subscribeChildChanges("/jike20", new ZkChildListener());
        Thread.sleep(Integer.MAX_VALUE);
    }
}
