package org.learning;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.CreateMode;

/**
 * Created by Anderson on 2018/9/13
 * 节点创建
 */
public class CreateNode {
    public static void main(String[] args) {
        ZkClient zc = new ZkClient("localhost:2181", 10000, 10000, new SerializableSerializer());
        System.out.println("conneted ok!");

        User u = new User();
        u.setId(1);
        u.setName("test");
        String path = zc.create("/jike/aa", u, CreateMode.PERSISTENT);
        System.out.println("created path:" + path);
    }
}
