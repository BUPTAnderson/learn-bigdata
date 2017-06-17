package org.learning.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.net.URI;

/**
 * delete(Path f, boolean recursive)
 * 当recursive=false，如果path是非空目录，会抛出异常： f is non empty
 * 其它情况下，不论recursive是true还是false，f都会被删除。
 * Created by anderson on 16-11-24.
 */
public class TestDelete
{
    public static final String uri = "hdfs://anderson-JD:9111";
    public static void main(String[] args)
            throws IOException, InterruptedException
    {
        // 获取Configuration 实例
        Configuration conf = new Configuration();
//        conf.set("fs.trash.interval", "1440");
        // HDFS 文件系统
        FileSystem fs = FileSystem.get(URI.create(uri), conf);
//        System.out.println(fs.deleteOnExit(new Path("/tmp")));
        // tmp是一个空目录， recursive=true /tmp会被删除
        // tmp是一个非空目录， recursive=true /tmp会被删除，里面的文件也会被删除
        // tmp是一个空目录， recursive=false /tmp会被删除
        // tmp是一个非空目录， recursive=false /tmp 不会被删除，并抛出异常，/tmp is non empty
//        System.out.println(fs.delete(new Path("/tmp"), false));
        // abc.txt是一个文件， recursive=true， 文件被删除
        // abc.txt是一个文件， recursive=false， 文件被删除
//        System.out.println(fs.delete(new Path("/abc.txt"), false));

        //这个是当当前程序的jvm退出是删除文件，调这个方法返回true，但是这时候去hdfs上看，文件还在，
        //20s后程序结束JVM退出，这时去HDFS上看，发现文件被删除了。
//        System.out.println(fs.deleteOnExit(new Path("/user/anderson/abc.txt")));
//        Thread.currentThread().sleep(20000L);

//        fs.moveFromLocalFile(new Path(""), new Path(""));
//        System.out.println(fs.mkdirs(new Path("/backup/anderson/databases")));
        //目标目录不存在的话，会失败
//        System.out.println(fs.rename(new Path("hdfs://anderson-JD:9111/abc.txt"), new Path("hdfs://anderson-JD:9111/backup/anderson/databases/abc.txt")));
//        System.out.println(fs.rename(new Path("/tmp"), new Path("/backup/anderson")));

        fs.mkdirs(new Path("/tmp"));

        fs.close();
    }
}
