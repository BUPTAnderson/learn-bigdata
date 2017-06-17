package org.learning.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.IOException;
import java.net.URI;

/**
 * Read a Sequence File
 * Created by anderson on 16-11-23.
 */
public class SequenceFileRead
{
    public static final String uri = "hdfs://anderson-JD:9111";
    public static void main(String[] args)
            throws IOException
    {
        // 获取Configuration 实例
        Configuration conf = new Configuration();
        // HDFS 文件系统
        FileSystem fs = FileSystem.get(URI.create(uri), conf);
        // 序列化文件路径
        Path path = new Path("/tmp1.seq");
        // Read SequenceFile
        SequenceFile.Reader reader = null;
        try {
            // Get Reader
            reader = new SequenceFile.Reader(fs, path, conf);
            // Get key/Value Class
            Writable key = (Writable) ReflectionUtils.newInstance(reader.getKeyClass(), conf);
            Writable value = (Writable) ReflectionUtils.newInstance(reader.getValueClass(), conf);
            // Read each key/value
            // 对应压缩的Sequence，不用改代码，会自动识别header中的压缩
            while (reader.next(key, value)) {
                System.out.println("key=" + key);
                System.out.println("value=" + value);
                //在文件中的偏移量
                System.out.println("position=" + reader.getPosition());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeStream(reader);
        }
    }
}
