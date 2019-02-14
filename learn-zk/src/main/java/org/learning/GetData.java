package org.learning;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.data.Stat;

/**
 * Created by Anderson on 2018/9/13
 */
public class GetData {
    public static void main(String[] args) {
        ZkClient zc = new ZkClient("localhost:2181", 10000, 10000, new SerializableSerializer());
        System.out.println("connected ok!");

        Stat stat = new Stat();
        User u = zc.readData("/jike5", stat);
        System.out.println(u.toString());
        System.out.println(stat);
    }
}
