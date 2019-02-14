package org.learning.balance;

import org.I0Itec.zkclient.ZkClient;

/**
 * Created by Anderson on 2018/9/18
 */
public class ZooKeeperRegistContext {
    private String path;
    private ZkClient zkClient;
    private Object data;

    public ZooKeeperRegistContext(String path, ZkClient zkClient, Object data) {
        super();
        this.path = path;
        this.zkClient = zkClient;
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ZkClient getZkClient() {
        return zkClient;
    }

    public void setZkClient(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
