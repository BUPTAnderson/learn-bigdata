package org.learning.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 *
 * 监控生产者:
 * java -Dcom.sun.management.jmxremote.port=9990 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.ext.dirs=lib -cp learn-kafka-1.0-SNAPSHOT.jar org.learning.producer.SimpleProducer
 * lib中jar包括:
 *  junit-4.4.jar  kafka_2.11-0.10.1.1.jar  kafka-clients-0.10.1.1.jar  log4j-1.2.17.jar  log4j-api-2.6.2.jar  log4j-core-2.4.1.jar  log4j-slf4j-impl-2.4.1.jar  slf4j-api-1.7.5.jar
 *
 * Created by anderson on 17-4-28.
 */
public class SimpleProducer
{
    public static void main(String[] args)
    {
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.177.78:9092,192.168.177.79:9092,192.168.177.80:9092");
//        props.put("bootstrap.servers", "192.168.178.37:9092,192.168.178.38:9092,192.168.178.80:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        int i = 0;
        while (true) {
            i = 0;
            while (i < 100) {
                producer.send(new ProducerRecord<String, String>("test", Integer.toString(i), "value:" + Integer.toString(i)));
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
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                producer.close();
            }
        }
    }
}
