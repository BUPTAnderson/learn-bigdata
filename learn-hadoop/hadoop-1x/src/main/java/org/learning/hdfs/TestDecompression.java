package org.learning.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.CompressionInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by kongyunlong on 2016/11/20.
 *
 * 运行该程序前提条件见TestCompression类
 *
 * 运行前设置参数
 * VM arguments:
 * -Djava.library.path=/home/anderson/hadoop-1.2.1/lib/native/Linux-amd64-64:/usr/local/lib
 */

public class TestDecompression {
    public static void main(String[] args) throws IOException {
//        String file = "/home/anderson/GitHub/shakespeare.json.snappy";
//        String file = "/home/anderson/GitHub/shakespeare.json.gz";
//        String file = "/home/anderson/GitHub/shakespeare.json.deflate";
        String file = "/home/anderson/GitHub/shakespeare.json.bz2";
        Configuration conf = new Configuration();
        CompressionCodecFactory codecFactory = new CompressionCodecFactory(conf);
        CompressionCodec codec = codecFactory.getCodec(new Path(file));
        CompressionInputStream in = codec.createInputStream(new FileInputStream(new File(file)));
        FileOutputStream out = new FileOutputStream(new File(codecFactory.removeSuffix(file, codec.getDefaultExtension())));
        IOUtils.copyBytes(in, out, 4096);
    }
}
