package org.learning.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.Text;

import java.io.IOException;
import java.net.URI;

/**
 * Created by anderson on 17-2-24.
 *
 * 该程序执行结束后
 * 创建目录 /io/tmp1.map， 并且该目录下面有两个文件：
 * -rw-r--r--   1 anderson supergroup       3228 2017-02-24 16:57 /io/tmp1.map/data
 * -rw-r--r--   1 anderson supergroup        136 2017-02-24 16:58 /io/tmp1.map/index
 *
 * data中是数据，并且key从小到大排列
 * index是索引， 结构如下：
 * 1    128
 * 散列的记录的Record的key值及该条Record在文件中的偏移量
 * 即MapFile是排序后的SequenceFile
 */
public class MapFileWrite
{
    public static final String uri = "hdfs://anderson-JD:9111";
    public static final String[] data = {
            "one, two, buckle my shoe",
            "three, four, shut the door",
            "five, six, pick up sticks",
            "seven, eight, lay them straight",
            "nine, ten, a big fat hen"
    };

    public static void main(String[] args)
            throws IOException
    {
        // 获取Configuration 实例
        Configuration conf = new Configuration();
        // HDFS 文件系统
        FileSystem fs = FileSystem.get(URI.create(uri), conf);
        String path = new String("/io/tmp1.map");
        IntWritable key = new IntWritable();
        Text value = new Text();
        MapFile.Writer writer = null;
        try {
            writer = new MapFile.Writer(conf, fs, path, key.getClass(), value.getClass());
            // 注意： 这里key的顺序必须从小到大，负责会报错！
            for (int i = 0; i < 100; i++) {
                key.set(i + 1);
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
