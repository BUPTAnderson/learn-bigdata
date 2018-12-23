package org.learning.mapreduce.case4;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Hadoop 案例4----平均成绩  简单问题  （入门级别）
 *
 * 1、需求分析
 *  对输入文件中数据进行就算学生平均成绩。输入文件中的每行内容均为一个学生的姓名和他相应的成绩，如果有多门学科，则每门学科为一个文件。
 *  要求在输出中每行有两个间隔的数据，其中，第一个代表学生的姓名，第二个代表其平均成绩。
 *
 * 2、原始数据
 * 1）math：
 * 张三    88
 * 李四    99
 * 王五    66
 * 赵六    77
 *
 * 2）china：
 * 张三    78
 * 李四    89
 * 王五    96
 * 赵六    67
 *
 * 3）english：
 * 张三    80
 * 李四    82
 * 王五    84
 * 赵六    86
 *
 * 样本输出：
 *  张三    82
 *  李四    90
 *  王五    82
 *  赵六    76
 *
 * 3、设计思考
 *  Map处理的 是一个纯文本文件， 文件中存放的数据时每一行表示一个学生的姓名和他相应一科成绩。Mapper处理的数据是由InputFormat分解过的数据集，
 *  其中 InputFormat的作用是将数据集切割成小数据集InputSplit，每一个InputSplit将由一个Mapper负责处理。此 外，InputFormat中还提供了一个RecordReader的实现，
 *  并将一个InputSplit解析成<key,value>对提 供给了map函数。InputFormat的默认值是TextInputFormat，它针对文本文件，按行将文本切割成InputSlit，
 *  并用 LineRecordReader将InputSplit解析成<key,value>对，key是行在文本中的位置，value是文件中的 一行。
 *
 *  Map的结果会通过partion分发到Reducer，Reducer做完Reduce操作后，将通过以格式OutputFormat输出。
 *
 *  Mapper最终处理的结果对<key,value>，会送到Reducer中进行合并，合并的时候，有相同key的键/值对则送到同一个 Reducer上。
 *  Reducer是所有用户定制Reducer类地基础，它的输入是key和这个key对应的所有value的一个迭代器，同时还有 Reducer的上下文。
 *  Reduce的结果由Reducer.Context的write方法输出到文件中。
 */
public class AvgJob {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        if (args.length != 2) {
            System.err.print("Usage: MaxTemperature<input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = new Job(conf, "avg_job");
        job.setJarByClass(AvgJob.class);

        job.setMapperClass(AvgMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setReducerClass(AvgReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

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
