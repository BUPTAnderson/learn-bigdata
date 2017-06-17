package org.learning.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.BZip2Codec;

import java.io.IOException;
import java.net.URI;

/**
 * Write a Sequence File
 * 执行后文件是写入到了hdfs集群中，不是在本地
 * Created by anderson on 16-11-23.
 */
public class SequenceFileWrite
{
    public static final String uri = "hdfs://anderson-JD:9111";
    public static final String[] data = {
        "one, two",
        "three, four",
        "five, six",
        "seven, eight",
        "nine, ten"
    };
    public static void main(String[] args)
            throws IOException
    {
        // 获取Configuration 实例
        Configuration conf = new Configuration();
        // HDFS 文件系统
        FileSystem fs = FileSystem.get(URI.create(uri), conf);
        // 序列化文件路径
        Path path = new Path("/id/tmp1.seq");
        // 打开SequenceFile.Write对象，写入键值
        SequenceFile.Writer writer = null;
        IntWritable key = new IntWritable();
        Text value = new Text();
        try {
            //不采用压缩
//            writer = SequenceFile.createWriter(fs, conf, path, key.getClass(), value.getClass());
            // 采用压缩
            writer = SequenceFile.createWriter(fs, conf, path, key.getClass(), value.getClass(), SequenceFile.CompressionType.RECORD, new BZip2Codec());

            // 采用snappy
//            String codecClassName = "org.apache.hadoop.io.compress.SnappyCodec";
//            Class<?> cls = Class.forName(codecClassName);
//            CompressionCodec codec = (CompressionCodec) ReflectionUtils.newInstance(cls, conf);
//            writer = SequenceFile.createWriter(fs, conf, path, key.getClass(), value.getClass(), SequenceFile.CompressionType.RECORD, codec);
            for (int i = 0; i < 100; i++) {
                key.set(100 - i);
                value.set(data[i % data.length]);
                writer.append(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeStream(writer);
        }
    }
}
