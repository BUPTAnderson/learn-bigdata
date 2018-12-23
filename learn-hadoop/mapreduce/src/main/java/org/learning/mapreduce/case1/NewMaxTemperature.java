package org.learning.mapreduce.case1;

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
 * ----Hadoop 实例1 通过采集的气象数据分析每年的最高温度
 * 1、原始数据分析
 * one.txt:
 * 0067011990999991950051507004888888889999999N9+00001+9999999999999999999999
 * 0067011990999991950051512004888888889999999N9+00221+9999999999999999999999
 * 0067011990999991950051518004888888889999999N9-00111+9999999999999999999999
 * 0067011990999991949032412004888888889999999N9+01111+9999999999999999999999
 * 0067011990999991950032418004888888880500001N9+00001+9999999999999999999999
 * 0067011990999991950051507004888888880500001N9+00781+9999999999999999999999
 *
 * cat two.txt
 * 0067011990999991951051507004888888889999999N9+99991+9999999999999999999999
 * 0067011990999991951051512004888888889999999N9+00231+9999999999999999999999
 * 0067011990999991951051518004888888889999999N9-00121+9999999999999999999999
 * 0067011990999991949032412004888888889999999N9+01101+9999999999999999999999
 * 0067011990999991952032418004888888880500001N9+00022+9999999999999999999999
 * 0067011990999991952051507004888888880500001N9+00751+9999999999999999999999
 *
 * cat three.txt
 * 0067011990999991952051507004888888889999999N9-00201+9999999999999999999999
 * 0067011990999991952051512004888888889999999N9+00121+9999999999999999999999
 * 0067011990999991952051518004888888889999999N9-00011+9999999999999999999999
 * 0067011990999991949032412004888888889999999N9+01211+9999999999999999999999
 * 0067011990999991951032418004888888880500001N9+00101+9999999999999999999999
 * 0067011990999991951051507004888888880500001N9+00281+9999999999999999999999
 *
 * 数据说明：
 *  第15-19个字符是year
 *  第45-50位是温度表示，+表示零上 -表示零下，且温度的值不能是9999，9999表示异常数据
 *  第50位值只能是0、1、4、5、9几个数字
 *
 * 5、打成jar，只需要打包对应的源代码即可，上传到/opt/mapred/job/1 目录下面 maxTemperature.jar
 *
 * 6、 $ hadoop fs -mkdir /user/anderson/input
 *     $ hadoop fs -put one.txt /user/anderson/input/
 *     $ hadoop fs -put two.txt /user/anderson/input/
 *     $ hadoop fs -put three.txt /user/anderson/input/
 *
 * 7、执行 hadoop jar /Users/anderson/IdeaProjects/learn-bigdata/learn-hadoop/mapreduce/target/mapreduce-1.0-SNAPSHOT.jar org.learning.mapreduce.case1.NewMaxTemperature /user/anderson/input /user/anderson/output
 *
 * 8、查看执行结果：
 * $ hadoop fs -cat /user/anderson/output/part-r-00000
 * 1949  121
 * 1950  78
 * 1951  28
 * 1952  75
 */
public class NewMaxTemperature {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        if (args.length != 2) {
            System.err.print("Usage: MaxTemperature<input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = new Job(conf, "max_temperature_job");
        job.setJarByClass(NewMaxTemperature.class);

        job.setMapperClass(NewMaxTemperatureMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setReducerClass(NewMaxTemperatureReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        Path outputDir = new Path(args[1]);
        FileSystem fs = FileSystem.get(conf);
        if (fs.exists(outputDir)) {
            fs.delete(outputDir, true);
            System.out.println("output dir exists, delete it.");
        }
        FileOutputFormat.setOutputPath(job, outputDir);
        System.out.println(job.waitForCompletion(true) ? 0 : 1);
    }
}
