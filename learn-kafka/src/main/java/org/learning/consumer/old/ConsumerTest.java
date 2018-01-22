package org.learning.consumer.old;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;

/**
 * Created by anderson on 18-1-20.
 */
public class ConsumerTest
        implements Runnable
{
    private KafkaStream mStream;
    private int mThreadNumber;

    public ConsumerTest(KafkaStream aStream, int aThreadNumber)
    {
        mThreadNumber = aThreadNumber;
        mStream = aStream;
    }

    public void run()
    {
        ConsumerIterator<byte[], byte[]> it = mStream.iterator();
        while (it.hasNext()) {
            MessageAndMetadata<byte[], byte[]> record = it.next();
            System.out.println("Thread " + mThreadNumber + ", key: " + new String(record.key()) + ", value:" + new String(it.next().message()));
        }
        System.out.println("Shutting down Thread: " + mThreadNumber);
    }
}
