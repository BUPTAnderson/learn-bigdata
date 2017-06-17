package org.learning.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by anderson on 16-11-19.
 * 参照SnappyCodec代码
 * 静态代码块：
 * LoadSnappy.isLoaded();
 * LoadSnappy类里面的静态代码块如下：
 *
 static {
     try {
         //在java路径中加载libsnappy.so
         System.loadLibrary("snappy");
         LOG.warn("Snappy native library is available");
         AVAILABLE = true;
     } catch (UnsatisfiedLinkError ex) {
         //NOP
     }
     //NativeCodeLoader类的静态代码块中会加载libhadoop.so
     boolean hadoopNativeAvailable = NativeCodeLoader.isNativeCodeLoaded();
     LOADED = AVAILABLE && hadoopNativeAvailable;
     if (LOADED) {
        LOG.info("Snappy native library loaded");
     } else {
        LOG.warn("Snappy native library not loaded");
     }
 }

 * 前提：
 * 编译native库：
 * $HADOOP_HOME下运行：ant compile-native
 * 编译成功后，生成native目录，(我的机器是64位)位置在：
 * $HADOOP_HOME/build/native/Linux-amd64-64
 * $HADOOP_HOME/build/native/Linux-amd64-64/lib/libhadoop.so
 * 想要使用native_hadoop,就要保证有上面的libhadoop.so文件
 *
 * 使用DEFLATE，gzip和Snappy压缩算法要安装Zlib和Snappy的库，（使用DEFLATE和gzip需要安装Zlib库，gzip以DEFLATE算法为基础）（使用Snappy需要安装Snappy库）
 *
 * 运行程序设置：
 * VM arguments:
 * -Djava.library.path=/home/anderson/hadoop-1.2.1/lib/native/Linux-amd64-64:/usr/local/lib
 */
public class TestCompression
{
    public static void main(String[] args)
            throws ClassNotFoundException, IOException
    {
        //snappy需要native库和libsnappy.so
        String codecClassName = "org.apache.hadoop.io.compress.SnappyCodec";    //shakespeare.json.snappy
        //BZip2Codec,自带java实现
//        String codecClassName = "org.apache.hadoop.io.compress.BZip2Codec"; //shakespeare.json.bz2
        //DefaultCodec压缩需要安装zlib,libhadoop.so, 因为默认压缩算法是deflate
//        String codecClassName = "org.apache.hadoop.io.compress.DefaultCodec";   //shakespeare.json.deflate
        //GzipCodec压缩是从DefaultCodec继承过来的
//        String codecClassName = "org.apache.hadoop.io.compress.GzipCodec"; // shakespeare.json.gz
        Class<?> cls = Class.forName(codecClassName);
        Configuration conf = new Configuration();
        CompressionCodec codec = (CompressionCodec) ReflectionUtils.newInstance(cls, conf);
        String inputFile = "/home/anderson/GitHub/shakespeare.json";
        String outFile = inputFile + codec.getDefaultExtension();
        FileOutputStream fileOut = new FileOutputStream(outFile);
        CompressionOutputStream out = codec.createOutputStream(fileOut);
        FileInputStream in = new FileInputStream(inputFile);
        IOUtils.copyBytes(in, out, 4096, false);
        in.close();
        out.close();
    }
}
