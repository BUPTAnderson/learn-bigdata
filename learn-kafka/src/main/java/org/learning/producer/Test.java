package org.learning.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * Created by anderson on 17-6-16.
 */
public class Test
{
    public static void main(String[] args)
    {
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.166.63:9092,192.168.166.65:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
//        for (int i = 0; i < 100; i++)
//        int i = 0;
//        while (i < 5000) {
            producer.send(new ProducerRecord<String, String>("order_produce_topic_k5", Integer.toString(1), "{\"appCode\":\"domain\",\"pin\":\"guhao002\",\"serviceCodes\":[\"domain\"],\"siteType\":0,\"sourceId\":\"883\",\"success\":true}"));
//            i++;
//        }

        producer.close();
    }
}
