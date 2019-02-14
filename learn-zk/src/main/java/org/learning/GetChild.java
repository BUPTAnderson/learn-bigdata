package org.learning;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import java.util.List;

/**
 * Created by Anderson on 2018/9/13
 * 获取子节点
 */
public class GetChild {
    public static void main(String[] args) {
        ZkClient zc = new ZkClient("localhost:2181", 10000, 10000, new SerializableSerializer());
        System.out.println("conneted ok!");

        List<String> cList = zc.getChildren("/jike5");

        System.out.println(cList.toString());
    }
}
