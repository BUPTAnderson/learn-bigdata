package org.learning.mapreduce.case3;

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
 * Hadoop 案例3----数据排序  简单问题  （入门级别）
 *
 * "数据排序"是许多实际任务执行时要完成的第一项工作，
 * 比如学生成绩评比、数据建立索引等。这个实例和数据去重类似，都是先对原始数据进行初步处理，为进一步的数据操作打好基础。下面进入这个示例。
 *
 * 1、需求描述
 *  对输入文件中数据进行排序。输入文件中的每行内容均为一个数字，即一个数据。
 *  要求在输出中每行有两个间隔的数字，其中，第一个代表原始数据在原始数据集中的位次，第二个代表原始数据。
 *
 * 2、原始数据
 *
 *  1）file1：
 * 2
 *
 * 32
 *
 * 654
 *
 * 32
 *
 * 15
 *
 * 756
 *
 * 65223
 *
 * 2）file2：
 * 5956
 *
 * 22
 *
 * 650
 *
 * 92
 *
 * 3）file3：
 *
 * 26
 *
 * 54
 *
 * 6
 *
 *     样例输出：
 *  1    2
 *  2    6
 *  3    15
 *  4    22
 *  5    26
 *  6    32
 *  7    32
 *  8    54
 *  9    92
 *  10    650
 *  11    654
 *  12    756
 *  13    5956
 *  14    65223
 *
 * 3、设计思考
 *  这个实例仅仅要求对输入数据进行排序，熟悉MapReduce过程的读者会很快想到在MapReduce过程中就有排序，是否可以利用这个默认的排序，
 *  而不需要自己再实现具体的排序呢？答案是肯定的。
 *
 *  但是在使用之前首先需要了解它的默认排序规则。它是按照key值进行排序的，如果key为封装int的IntWritable类型，那么MapReduce按照数字大小对key排序，
 *  如果key为封装为String的Text类型，那么MapReduce按照字典顺序对字符串排序。
 *
 *  了解了这个细节，我们就知道应该使用封装int的IntWritable型数据结构了。也就是在map中将读入的数据转化成 IntWritable型，然后作为key值输出（value任意）。
 *  reduce拿到<key，value-list>之后，将输入的 key作为value输出，并根据value-list中元素的个数决定输出的次数。
 *  输出的key（即代码中的linenum）是一个全局变量，它统计当前key的位次。需要注意的是这个程序中没有配置Combiner，也就是在MapReduce过程中不使用Combiner。
 *  这主要是因为使用map和reduce就已经能够完成任务了。
 */
public class SortJob {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        if (args.length != 2) {
            System.err.print("Usage: MaxTemperature<input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = new Job(conf, "sort_job");

        job.setJarByClass(SortJob.class);

        job.setMapperClass(SortMapper.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Text.class);

        job.setReducerClass(SortReducer.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileSystem fs = FileSystem.get(conf);
        Path output = new Path(args[1]);
        if (fs.exists(output)) {
            fs.delete(output, true);
            System.out.println("output dir exists, delete it");
        }
        FileOutputFormat.setOutputPath(job, output);

        System.out.println(job.waitForCompletion(true) ? 0 : 1);
    }
}
