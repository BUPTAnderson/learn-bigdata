/**
 * @author Geloin
 */
package org.learning.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * ES工具类
 * 注意：在其它项目里引入ES工具类的时候，注意以下几点：
 * 必要包的引入：
 * <p>
 * <!-- elasticsearch -->
 * <!-- https://mvnrepository.com/artifact/org.elasticsearch/elasticsearch -->
 * <dependency>
 * <groupId>org.elasticsearch</groupId>
 * <artifactId>elasticsearch</artifactId>
 * <version>5.0.1</version>
 * </dependency>
 * <!-- https://mvnrepository.com/artifact/org.elasticsearch.client/transport -->
 * <dependency>
 * <groupId>org.elasticsearch.client</groupId>
 * <artifactId>transport</artifactId>
 * <version>5.0.1</version>
 * </dependency>
 * <!-- https://mvnrepository.com/artifact/io.netty/netty-all -->
 * <dependency>
 * <groupId>io.netty</groupId>
 * <artifactId>netty-all</artifactId>
 * <version>4.1.6.Final</version>
 * </dependency>
 * <!-- elasticsearch end -->
 * <p>
 * java Client使用的是9300端口！
 * <p>
 * TransportClient 
 * 不加入集群，只是连接集群，更轻量级
 * 将请求轮询发送到所有配置的ES节点
 * 可以通过查询Cluster state API动态更新节点信息
 * 不开启自动嗅探功能，只会轮询的把请求发送给我们在client中配置的节点（即通过addTransportAddress方法加入的节点），
 * 如果开启的自动嗅探功能，会轮询的发送给集群中的节点。
 */
public class ElasticSearchUtil {
    private static final Logger log = LoggerFactory.getLogger(ElasticSearchUtil.class);

    /**
     * 索引库是否存在
     *
     * @param index 索引库名
     * @return 存在则返回true，不存在则返回false
     */
    public static Boolean indexExist(String index) {
        Client client = createClient();
        IndicesExistsResponse exist = client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet();
        System.out.println("index:" + index + ", exist:" + exist.isExists());
        client.close();
        return exist.isExists();
    }

    /**
     * 生成客户端
     *
     * @return 客户端 {@link Client}
     */
    public static Client createClient() {
        InputStream fis = ElasticSearchUtil.class.getClassLoader().getResourceAsStream("config.properties");
        Properties properties = new Properties();
        try {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String clusterName = properties.getProperty("cluster.name");
        String hosts = properties.getProperty("es.url");
        Settings settings = Settings.builder().put("cluster.name", clusterName).put("client.transport.sniff", true).build();
        TransportClient transportClient = new PreBuiltTransportClient(settings);
        String[] hostsArr = hosts.split(",");
        for (String host : hostsArr) {
            if (hosts != null) {
                String[] splits = host.split(":");
                String h = splits[0];
                Integer p = null;
                if (splits.length == 1) {
                    p = 9300;
                } else {
                    p = Integer.parseInt(splits[1]);
                }
                try {
                    transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(h), p));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }
        Client client = transportClient;
        System.out.println("--- initClient finish ---");
        return client;
    }

    /**
     * 将Bean转化为JSon
     *
     * @param obj 要转化的Bean
     * @return 转化后的结果
     */
    public static String beanToJson(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new String();
        }
    }
}
