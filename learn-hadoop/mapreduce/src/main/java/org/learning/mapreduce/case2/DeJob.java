package org.learning.mapreduce.case2;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Hadoop 案例2----数据去重问题  简单问题  （入门级别）
 *
 * 1、原始数据
 * 1）file1：
 * 2012-3-1 a
 * 2012-3-2 b
 * 2012-3-3 c
 * 2012-3-4 d
 * 2012-3-5 a
 * 2012-3-6 b
 * 2012-3-7 c
 * 2012-3-3 c
 *
 * 2）file2：
 * 2012-3-1 b
 * 2012-3-2 a
 * 2012-3-3 b
 * 2012-3-4 d
 * 2012-3-5 a
 * 2012-3-6 c
 * 2012-3-7 d
 * 2012-3-3 c
 *
 * 数据输出：
 *     2012-3-1 a
 *     2012-3-1 b
 *     2012-3-2 a
 *     2012-3-2 b
 *     2012-3-3 b
 *     2012-3-3 c
 *     2012-3-4 d
 *     2012-3-5 a
 *     2012-3-6 b
 *     2012-3-6 c
 *     2012-3-7 c
 *     2012-3-7 d
 *
 * 2、说明
 *  数据去重的最终目标是让原始数据中出现次数超过一次的数据在输出文件中只出现一次。我们自然而然会想到将同一个数据的所有记录都交给一台reduce机器，
 *  无论这个数据出现多少次，只要在最终结果中输出一次就可以了。具体就是reduce的输入应该以数据作为key，
 *  而对value-list则没有要求。当reduce接收到一个<key，value-list>时就直接将key复制到输出的key中，并将value设置成空值。
 */
public class DeJob {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        if (args.length != 2) {
            System.err.print("Usage: MaxTemperature<input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = new Job(conf, "de_job");
        job.setJarByClass(DeJob.class);

        job.setMapperClass(DeMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setReducerClass(DeReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileSystem fs = FileSystem.get(conf);
        Path output = new Path(args[1]);
        if (fs.exists(output)) {
            System.out.println("output dir exists, delete it.");
            fs.delete(output, true);
        }
        FileOutputFormat.setOutputPath(job, output);

        System.out.println(job.waitForCompletion(true) ? 0 : 1);
    }
}
