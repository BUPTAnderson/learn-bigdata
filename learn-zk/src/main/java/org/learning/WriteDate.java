package org.learning;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

/**
 * Created by Anderson on 2018/9/13
 * 数据修改
 */
public class WriteDate {
    public static void main(String[] args) {
        ZkClient zc = new ZkClient("localhost:2181", 10000, 10000, new SerializableSerializer());
        System.out.println("conneted ok!");

        User u = new User();
        u.setId(2);
        u.setName("test7");
        // 节点必须存在, 并且节点version(dataVersion)必须是0才能修改成功
        zc.writeData("/jike20", u, 7);
    }
}
