package org.learning.mapreduce.case7;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Hadoop 案例7-----日志分析：分析非结构化文件
 *
 * 1、需求：根据tomcat日志计算url访问了情况，具体的url如下，
 *  要求：区别统计GET和POST URL访问量
 *  结果为：访问方式、URL、访问量
 * 127.0.0.1 - - [03/Jul/2014:23:36:38 +0800] "GET /course/detail/3.htm HTTP/1.0" 200 38435 0.038
 * 182.131.89.195 - - [03/Jul/2014:23:37:43 +0800] "GET / HTTP/1.0" 301 - 0.000
 * 127.0.0.1 - - [03/Jul/2014:23:38:27 +0800] "POST /service/notes/addViewTimes_23.htm HTTP/1.0" 200 2 0.003
 * 127.0.0.1 - - [03/Jul/2014:23:39:03 +0800] "GET /html/notes/20140617/779.html HTTP/1.0" 200 69539 0.046
 * 127.0.0.1 - - [03/Jul/2014:23:43:00 +0800] "GET /html/notes/20140318/24.html HTTP/1.0" 200 67171 0.049
 * 127.0.0.1 - - [03/Jul/2014:23:43:59 +0800] "POST /service/notes/addViewTimes_779.htm HTTP/1.0" 200 1 0.003
 * 127.0.0.1 - - [03/Jul/2014:23:45:51 +0800] "GET / HTTP/1.0" 200 70044 0.060
 * 127.0.0.1 - - [03/Jul/2014:23:46:17 +0800] "GET /course/list/73.htm HTTP/1.0" 200 12125 0.010
 * 127.0.0.1 - - [03/Jul/2014:23:46:58 +0800] "GET /html/notes/20140609/542.html HTTP/1.0" 200 94971 0.077
 * 127.0.0.1 - - [03/Jul/2014:23:48:31 +0800] "POST /service/notes/addViewTimes_24.htm HTTP/1.0" 200 2 0.003
 * 127.0.0.1 - - [03/Jul/2014:23:48:34 +0800] "POST /service/notes/addViewTimes_542.htm HTTP/1.0" 200 2 0.003
 * 127.0.0.1 - - [03/Jul/2014:23:49:31 +0800] "GET /notes/index-top-3.htm HTTP/1.0" 200 53494 0.041
 * 127.0.0.1 - - [03/Jul/2014:23:50:55 +0800] "GET /html/notes/20140609/544.html HTTP/1.0" 200 183694 0.076
 * 127.0.0.1 - - [03/Jul/2014:23:53:32 +0800] "POST /service/notes/addViewTimes_544.htm HTTP/1.0" 200 2 0.004
 * 127.0.0.1 - - [03/Jul/2014:23:54:53 +0800] "GET /html/notes/20140620/900.html HTTP/1.0" 200 151770 0.054
 * 127.0.0.1 - - [03/Jul/2014:23:57:42 +0800] "GET /html/notes/20140620/872.html HTTP/1.0" 200 52373 0.034
 * 127.0.0.1 - - [03/Jul/2014:23:58:17 +0800] "POST /service/notes/addViewTimes_900.htm HTTP/1.0" 200 2 0.003
 */
public class LogJob {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        if (args.length != 2) {
            System.err.print("Usage: MaxTemperature<input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = new Job(conf, "log_job");
        job.setJarByClass(LogJob.class);

        job.setMapperClass(LogMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setReducerClass(LogReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileSystem fs = FileSystem.get(conf);
        Path output = new Path(args[1]);
        if (fs.exists(output)) {
            fs.delete(output, true);
            System.out.println("output dir exists, delete it.");
        }
        FileOutputFormat.setOutputPath(job, output);

        System.out.println(job.waitForCompletion(true) ? 0 : 1);
    }
}
