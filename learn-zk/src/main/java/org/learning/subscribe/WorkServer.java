package org.learning.subscribe;

import com.alibaba.fastjson.JSON;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

/**
 * 代表工作服务器
 * Created by Anderson on 2018/9/18
 */
public class WorkServer {
    private ZkClient zkClient;
    // ZooKeeper
    private String configPath;
    // ZooKeeper集群中servers节点的路径
    private String serversPath;
    // 当前工作服务器的基本信息
    private ServerData serverData;
    // 当前工作服务器的配置信息
    private ServerConfig serverConfig;
    private IZkDataListener dataListener;

    public WorkServer(String configPath, String serversPath,
                      ServerData serverData, ZkClient zkClient, ServerConfig initConfig) {
        this.zkClient = zkClient;
        this.serversPath = serversPath;
        this.configPath = configPath;
        this.serverConfig = initConfig;
        this.serverData = serverData;

        this.dataListener = new IZkDataListener() {
            public void handleDataDeleted(String dataPath) throws Exception {
            }

            public void handleDataChange(String dataPath, Object data)
                    throws Exception {
                String retJson = new String((byte[]) data);
                ServerConfig serverConfigLocal = (ServerConfig) JSON.parseObject(retJson, ServerConfig.class);
                updateConfig(serverConfigLocal);
                System.out.println("new Work server config is:" + serverConfig.toString());
            }
        };
    }

    // 启动服务器
    public void start() {
        System.out.println("work server start...");
        initRunning();
    }

    // 停止服务器
    public void stop() {
        System.out.println("work server stop...");
        zkClient.unsubscribeDataChanges(configPath, dataListener); // 取消监听config节点
    }

    // 服务器初始化
    private void initRunning() {
        registMe(); // 注册自己
        zkClient.subscribeDataChanges(configPath, dataListener); // 订阅config节点的改变事件
    }

    // 启动时向zookeeper注册自己的注册函数
    private void registMe() {
        String mePath = serversPath.concat("/").concat(serverData.getAddress());

        try {
            zkClient.createEphemeral(mePath, JSON.toJSONString(serverData)
                    .getBytes());
        } catch (ZkNoNodeException e) {
            zkClient.createPersistent(serversPath, true);
            registMe();
        }
    }

    // 更新自己的配置信息
    private void updateConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }
}
