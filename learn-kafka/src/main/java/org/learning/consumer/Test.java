package org.learning.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

/**
 * Created by anderson on 17-6-16.
 */
public class Test
{
    public static void main(String[] args)
    {
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.177.78:9092,192.168.177.79:9092,192.168.177.80:9092");
        props.put("zookeeper.connect", "bds-test-002:2182,bds-test-003:2182,bds-test-004:2182");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
//        consumer.subscribe(Arrays.asList("foo", "bar"));  // 一个消费者可以同时消费多个topic
        consumer.subscribe(Arrays.asList("kyl-test"));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records)
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
        }
    }
}
