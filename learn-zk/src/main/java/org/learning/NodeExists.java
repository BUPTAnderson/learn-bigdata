package org.learning;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

/**
 * Created by Anderson on 2018/9/13
 */
public class NodeExists {
    public static void main(String[] args) {
        ZkClient zc = new ZkClient("localhost:2181", 10000, 10000, new SerializableSerializer());
        System.out.println("connected ok!");

        boolean e = zc.exists("/jike5");

        System.out.println(e);
    }
}
