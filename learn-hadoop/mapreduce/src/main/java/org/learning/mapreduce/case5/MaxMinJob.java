package org.learning.mapreduce.case5;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Hadoop 案例5-----求最大最小值问题：SELECT MAX(NUMBER),MIN(NUMBER) FROM TABLE 获取某列的最大值和最小值
 *
 * 1、数据准备
 * [root@x00 hd]# cat eightteen_a.txt
 * 102
 * 10
 * 39
 * 109
 * 200
 * 11
 * 3
 * 90
 * 28
 * [root@x00 hd]# cat eightteen_b.txt
 * 5
 * 2
 * 30
 * 838
 * 10005
 *
 * 结果预测
 * Max 10005
 * Min 2
 */
public class MaxMinJob {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        if (args.length != 2) {
            System.err.print("Usage: MaxTemperature<input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = new Job(conf, "maxmin_job");
        job.setJarByClass(MaxMinJob.class);

        job.setMapperClass(MaxMinMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);

        job.setReducerClass(MaxMinReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

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
