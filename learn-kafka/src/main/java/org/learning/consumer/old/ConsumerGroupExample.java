package org.learning.consumer.old;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Consumer 低级API 之 High Level
 * 该实例来自官方网站, 链接: https://cwiki.apache.org/confluence/display/KAFKA/Consumer+Group+Example
 * 该实例包括ConsumerGroupExample和ConsumerTest
 *
 * 启动main方法传入三个参数:
 * zooKeeper, 示例:bds-test-002:2182,bds-test-003:2182,bds-test-004:2182/kafka-dev
 * groupId, 示例: group-test
 * topic, 示例：　test
 * threads, 示例: 3
 *
 *
 * 不过要注意一些注意事项，对于多个partition和多个consumer
 * 1. 如果consumer比partition多，是浪费，因为kafka的设计是在一个partition上是不允许并发的，所以consumer数不要大于partition数
 * 2. 如果consumer比partition少，一个consumer会对应于多个partitions，这里主要合理分配consumer数和partition数，否则会导致partition里面的数据被取的不均匀
 * 最好partiton数目是consumer数目的整数倍，所以partition数目很重要，比如取24，就很容易设定consumer数目
 * 3. 如果consumer从多个partition读到数据，不保证数据间的顺序性，kafka只保证在一个partition上数据是有序的，但多个partition，根据你读的顺序会有不同
 * 4. 增减consumer，broker，partition会导致rebalance，所以rebalance后consumer对应的partition会发生变化
 * 5. High-level接口中获取不到数据的时候是会block的
 *
 * Created by anderson on 18-1-20.
 */
public class ConsumerGroupExample
{
    private final ConsumerConnector consumer;
    private final String topic;
    private ExecutorService executor;

    public ConsumerGroupExample(String aZookeeper, String aGroupId, String aTopic)
    {
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(
                createConsumerConfig(aZookeeper, aGroupId));
        this.topic = aTopic;
    }

    public void shutdown()
    {
        if (consumer != null) {
            consumer.shutdown();
        }
        if (executor != null) {
            executor.shutdown();
        }
        try {
            if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                System.out.println("Timed out waiting for consumer threads to shut down, exiting uncleanly");
            }
        }
        catch (InterruptedException e) {
            System.out.println("Interrupted during shutdown, exiting uncleanly");
        }
    }

    public void run(int aNumThreads)
    {
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(aNumThreads));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        // now launch all the threads
        //
        executor = Executors.newFixedThreadPool(aNumThreads);

        // now create an object to consume the messages
        //
        int threadNumber = 0;
        for (final KafkaStream stream : streams) {
            executor.submit(new ConsumerTest(stream, threadNumber));
            threadNumber++;
        }
    }

    private static ConsumerConfig createConsumerConfig(String aZookeeper, String aGroupId)
    {
        Properties props = new Properties();
        props.put("zookeeper.connect", aZookeeper);
        props.put("group.id", aGroupId);
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");

        return new ConsumerConfig(props);
    }

    public static void main(String[] args)
    {
        String zooKeeper = args[0];
        String groupId = args[1];
        String topic = args[2];
        int threads = Integer.parseInt(args[3]);

        ConsumerGroupExample example = new ConsumerGroupExample(zooKeeper, groupId, topic);
        example.run(threads);

        try {
            Thread.sleep(100000L);
        }
        catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        example.shutdown();
    }
}
