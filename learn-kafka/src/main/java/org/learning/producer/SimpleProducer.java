package org.learning.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * Created by anderson on 17-4-28.
 */
public class SimpleProducer
{
    public static void main(String[] args)
    {
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.177.80:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
//        for (int i = 0; i < 100; i++)
        int i = 0;
        while (i < 5000) {
            producer.send(new ProducerRecord<String, String>("kyl-topic", Integer.toString(i), Integer.toString(i)));
            i++;
        }

        producer.close();
    }
}
