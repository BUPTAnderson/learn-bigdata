package org.learning.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Created by anderson on 16-11-19.
 */
public class TestHDFS
{
    public static String hdfsUrl = "hdfs://localhost:9100";

    /**
     * create HDFS dir
     * @throws IOException
     */
    public void testHDFSMkdir() throws IOException {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(hdfsUrl), conf);
        Path path = new Path("/test");
        fs.mkdirs(path);
    }

    /**
     * create a file
     * @throws IOException
     */
    public void testCreateFile() throws IOException {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(hdfsUrl), conf);
        Path path = new Path("/test/a.txt");
        FSDataOutputStream out = fs.create(path);
        out.write("hello hadoop!".getBytes());
    }

    /**
     * rename a file
     * @throws IOException
     */
    public void testRenameFile() throws IOException {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(hdfsUrl), conf);
        Path path = new Path("/test/a.txt");
        Path newPath = new Path("/test/b.txt");
        System.out.println(fs.rename(path, newPath));
    }

    /**
     * upload a local file
     * @throws IOException
     */
    public void testUploadLocalFile1() throws IOException {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(hdfsUrl), conf);
        Path src = new Path("/Users/anderson/IdeaProjects/hadoop-1.2.1/bin/rcc");
        Path dst = new Path("/test");
        fs.copyFromLocalFile(src, dst);
    }

    /**
     * upload a local file
     * @throws IOException
     */
    public void testUploadLocalFile2() throws IOException {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(hdfsUrl), conf);
        InputStream in = new BufferedInputStream(new FileInputStream("/Users/anderson/IdeaProjects/hadoop-1.2.1/bin/rcc"));
//        FSDataOutputStream out = fs.create(new Path("/test/rcc1"));
        // 输出提示信息
        FSDataOutputStream out = fs.create(new Path("/test/rcc2"), new Progressable() {
            @Override
            public void progress() {
                System.out.println("----------------------------");
            }
        });
        IOUtils.copyBytes(in, out, 4096);
    }

    /**
     * list files under folder
     */
    public void testListFiles() throws IOException {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(hdfsUrl), conf);
//        Path path = new Path("/test");
        Path path = new Path("/examples/iptables_new/20171201115005");
        FileStatus[] files = fs.listStatus(path);
        for (FileStatus file : files) {
            System.out.println(file.getPath().toString() + "," + file.getPath().getName());
        }
    }

    /**
     * list block info of file
     * @throws IOException
     */
    public void testGetBlockInfo() throws IOException {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(hdfsUrl), conf);
        Path path = new Path("/test/rcc");
        FileStatus fileStatus = fs.getFileStatus(path);
        BlockLocation[] blkLocs = fs.getFileBlockLocations(fileStatus, 0, fileStatus.getLen());
        for (BlockLocation blockLocation : blkLocs) {
            for (String host : blockLocation.getHosts()) {
                System.out.println(host);
            }
        }
    }
    public static void main(String[] args) throws IOException {
       TestHDFS testHDFS = new TestHDFS();
//       testHDFS.testHDFSMkdir();
//        testHDFS.testCreateFile();
//        testHDFS.testRenameFile();
//        testHDFS.testUploadLocalFile1();
//        testHDFS.testUploadLocalFile2();
        testHDFS.testListFiles();
//        testHDFS.testGetBlockInfo();
    }
}