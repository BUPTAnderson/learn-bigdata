package org.learning.mapreduce.case9;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * ----Hadoop 实例9  Join讲解2: 将人员的地址ID完善成为地址名称，输出格式要求：人员Id，姓名，地址
 *
 * 1、原始数据
 *  人员ID    人员名称    地址ID
 * 1    张三  1
 * 2    李四  2
 * 3    王五  1
 * 4    赵六  3
 * 5    马七  3
 *
 *  另外一组为地址信息:
 *  地址ID    地址名称
 * 1    北京
 * 2    上海
 * 3    广州
 *
 * 2、处理说明
 *  这里给出了一个很简单的例子,而且数据量很小,就这么用眼睛就能看过来的几行,当然,实际的情况可能是几十万上百万甚至上亿的数据量.
 *  要实现的功能很简单, 就是将人员信息与地址信息进行join,将人员的地址ID完善成为地址名称.
 *  对于Hadoop文件系统的应用,目前看来,很多数据的存储都是基于文本的, 而且都是将数据放在一个文件目录中进行处理.因此我们这里也采用这种模式来完成.
 *
 *  对于mapreduce程序来说,最主要的就是将要做的工作转化为map以及reduce两个部分.
 *  我们可以将地址以及人员都采用同样的数据结构来存储,通 过一个flag标志来指定该数据结构里面存储的是地址信息还是人员信息.
 *  经过map后,使用地址ID作为key,将所有的具有相同地址的地址信息和人员信 息放入一个key->value list数据结构中传送到reduce中进行处理.
 *  在reduce过程中,由于key是地址的ID,所以value list中只有一个是地址信息,其他的都是人员信息,因此,找到该地址信息后,其他的人员信息的地址就是该地址所指定的地址名称.
 *
 *  OK,我们的join算法基本搞定啦.剩下就是编程实现了,let’s go.
 */
public class JoinTwoJob {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        Job job = new Job(configuration, "join_two_job");
        job.setJarByClass(JoinTwoJob.class);

        job.setMapperClass(JoinTwoMapper.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(User.class);

        job.setReducerClass(JoinTwoReducer.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        Path path = new Path(args[1]);
        FileSystem fs = FileSystem.get(configuration);
        if (fs.exists(path)) {
            fs.delete(path, true);
        }
        FileOutputFormat.setOutputPath(job, path);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
