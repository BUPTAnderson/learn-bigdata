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
 */
public class MapFileRead
{
    public static final String uri = "hdfs://anderson-JD:9111";

    public static void main(String[] args)
            throws IOException
    {
        // 获取Configuration 实例
        Configuration conf = new Configuration();
        // HDFS 文件系统
        FileSystem fs = FileSystem.get(URI.create(uri), conf);
        String path = new String("/io/tmp1.map");
        MapFile.Reader reader = null;
        try {
            reader = new MapFile.Reader(fs, path, conf);
            IntWritable key = new IntWritable();
            Text value = new Text();
            while (reader.next(key, value)) {
                System.out.print("key=" + key + " ,");
                System.out.println("value=" + value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeStream(reader);
        }
    }
}
