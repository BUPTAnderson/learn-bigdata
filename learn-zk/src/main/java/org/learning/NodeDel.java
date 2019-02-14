package org.learning;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

/**
 * Created by Anderson on 2018/9/13
 * 节点删除
 */
public class NodeDel {
    public static void main(String[] args) {
        ZkClient zc = new ZkClient("localhost:2181", 10000, 10000, new SerializableSerializer());
        System.out.println("conneted ok!");

        boolean e1 = zc.delete("/jike5");
        boolean e2 = zc.deleteRecursive("/jike5");
    }
}
