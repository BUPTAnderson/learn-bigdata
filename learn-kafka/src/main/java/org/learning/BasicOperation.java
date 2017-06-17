package org.learning;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.server.ConfigType;
import kafka.utils.ZkUtils;
import org.apache.kafka.common.requests.MetadataResponse;
import org.apache.kafka.common.security.JaasUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by anderson on 17-4-28.
 * 参看链接：http://www.cnblogs.com/huxi2b/p/6592862.html
 */
public class BasicOperation
{
    public static void main(String[] args)
    {
        // 创建topic
//        createTopic();
        // 删除topic
//        deleteTopic();
        // 查询topic properties
//        getTopicProps();
        // 查询topic metadata
//        getTopicMetadata();
        // 修改topic
//        modifyTopic();
    }

    // 创建topic
    public static void createTopic()
    {
        ZkUtils zkUtils = ZkUtils.apply("192.168.177.80:2182", 30000, 30000, JaasUtils.isZkSecurityEnabled());
        // 创建一个单分区单副本名为t1的topic
        AdminUtils.createTopic(zkUtils, "kyl-test", 3, 2, new Properties(), RackAwareMode.Enforced$.MODULE$);
        zkUtils.close();
    }

    // 删除topic
    public static void deleteTopic()
    {
        ZkUtils zkUtils = ZkUtils.apply("192.168.177.80:2182", 30000, 30000, JaasUtils.isZkSecurityEnabled());
        // 删除topic 't1'
        AdminUtils.deleteTopic(zkUtils, "kyl-test");
        zkUtils.close();
    }

    // 查询topic properties
    public static void getTopicProps()
    {
        ZkUtils zkUtils = ZkUtils.apply("192.168.177.80:2182", 30000, 30000, JaasUtils.isZkSecurityEnabled());
        // 获取topic 'test'的topic属性属性
        Properties props = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), "kyl-test");

        // 查询topic-level属性
        Iterator it = props.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            System.out.println(key + " = " + value);
        }

        zkUtils.close();
    }

    // 查询topic metadata
    public static void getTopicMetadata()
    {
        ZkUtils zkUtils = ZkUtils.apply("192.168.177.80:2182", 30000, 30000, JaasUtils.isZkSecurityEnabled());
        MetadataResponse.TopicMetadata topicMetadata = AdminUtils.fetchTopicMetadataFromZk("kyl-test", zkUtils);
        System.out.println("PartitionCount:" + topicMetadata.partitionMetadata().size());
        for (MetadataResponse.PartitionMetadata partitionMetadata : topicMetadata.partitionMetadata()) {
            System.out.println("Partition:" + partitionMetadata.partition() + ", Leader:" + partitionMetadata.leader() + ", Replicas:" + partitionMetadata.replicas() + ", Isr:" + partitionMetadata.isr());
        }
        zkUtils.close();
    }

    // 修改topic
    public static void modifyTopic()
    {
        ZkUtils zkUtils = ZkUtils.apply("localhost:2181", 30000, 30000, JaasUtils.isZkSecurityEnabled());
        Properties props = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), "test");
        // 增加topic级别属性
        props.put("min.cleanable.dirty.ratio", "0.3");
        // 删除topic级别属性
        props.remove("max.message.bytes");
        // 修改topic 'test'的属性
        AdminUtils.changeTopicConfig(zkUtils, "test", props);
        zkUtils.close();
    }
}
