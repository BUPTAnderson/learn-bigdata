package org.learning.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.ChecksumFileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

/**
 * Created by kongyunlong on 2016/11/20.
 */
public class TestChecksumFileSystem {
    public static void main(String[] args) throws IOException {
        Configuration conf = new Configuration();
        LocalFileSystem localFS = ChecksumFileSystem.getLocal(conf);
        // 输出：/home/anderson/GitHub/.shakespeare.json.crc
        System.out.println(localFS.getChecksumFile(new Path("/home/anderson/GitHub/shakespeare.json")));
    }
}
