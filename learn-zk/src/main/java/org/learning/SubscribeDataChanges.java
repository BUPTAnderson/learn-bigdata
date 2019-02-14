package org.learning;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by Anderson on 2018/9/13
 */
public class SubscribeDataChanges {
    private static class ZkDataListener implements IZkDataListener {
        public void handleDataChange(String dataPath, Object data)
                throws Exception {
            System.out.println(data.getClass());
            if (data instanceof byte[]) {
                System.out.println("----------------");
                Object o = byteArrayToObject((byte[]) data);
                System.out.println(o.getClass());
                if (o instanceof User) {
                    System.out.println(dataPath + ":" + ((User) o).toString());
                } else {
                    System.out.println(dataPath + ":" + data.toString());
                }
            } else {
                System.out.println(dataPath + ":" + data.toString());
            }
        }

        public void handleDataDeleted(String dataPath) throws Exception {
            System.out.println(dataPath);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ZkClient zc = new ZkClient("localhost:2181", 10000, 10000, new BytesPushThroughSerializer());
        System.out.println("conneted ok!");

        zc.subscribeDataChanges("/jike20", new ZkDataListener());
        Thread.sleep(Integer.MAX_VALUE);
    }

    public static Object byteArrayToObject(byte[] bytes) {
        Object obj = null;
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            obj = objectInputStream.readObject();
        } catch (Exception e) {
            System.out.println("byteArrayToObject failed");
        } finally {
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException e) {
                    System.out.println("close byteArrayInputStream failed");
                }
            }
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    System.out.println("close objectInputStream failed");
                }
            }
        }
        return obj;
    }
}
