package org.learning.mapreduce.case6;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Hadoop 案例6-----TopN问题：求最大的K个值并排序
 *
 * 1、需求分析
 *
 * #orderid,userid,payment,productid
 * [root@x00 hd]# cat seventeen_a.txt
 * 1,9819,100,121
 * 2,8918,2000,111
 * 3,2813,1234,22
 * 4,9100,10,1101
 * 5,3210,490,111
 * 6,1298,28,1211
 * 7,1010,281,90
 * 8,1818,9000,20
 * [root@x00 hd]# cat seventeen_b.txt
 * 100,3333,10,100
 * 101,9321,1000,293
 * 102,3881,701,20
 * 103,6791,910,30
 * 104,8888,11,39
 *
 * 预测结果：（求 Top N=5 的结果）
 * 1    9000
 * 2    2000
 * 3    1234
 * 4    1000
 * 5    910
 */
public class TopNJob {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        if (args.length != 3) {
            System.err.print("Usage: MaxTemperature<input path> <output path> <TopN>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        conf.setInt("N", Integer.parseInt(args[2]));
        Job job = new Job(conf, "topN_job");
        job.setJarByClass(TopNJob.class);

        job.setMapperClass(TopNMapper.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setReducerClass(TopNReducer.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileSystem fs = FileSystem.get(conf);
        Path output = new Path(args[1]);
        if (fs.exists(output)) {
            fs.delete(output, true);
            System.out.println("output dir exists, deltet it.");
        }
        FileOutputFormat.setOutputPath(job, output);

        System.out.println(job.waitForCompletion(true) ? 0 : 1);
    }
}
