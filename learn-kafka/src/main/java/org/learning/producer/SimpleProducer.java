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
        props.put("bootstrap.servers", "192.168.177.78:9092,192.168.177.79:9092,192.168.177.80:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        int i = 0;
        while (i < 5000) {
            producer.send(new ProducerRecord<String, String>("D879637D5FBB4DCDAD451B3F4C2826A6", Integer.toString(i), Integer.toString(i)));
            i++;
            // 指定分区
//            String key = Integer.toString(i);
//            String value = Integer.toString(i);
//            producer.send(new ProducerRecord<String, String>("D879637D5FBB4DCDAD451B3F4C2826A6", 8, key, value), new Callback() {
//                public void onCompletion(RecordMetadata metadata, Exception e) {
//                    if(e != null) {
//                        e.printStackTrace();
//                    } else {
//                        System.out.println("The offset of the record we just sent is: " + metadata.offset());
//                    }
//                }
//            });
        }
        producer.close();
    }
}
