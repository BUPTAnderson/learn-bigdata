package org.learning.mapreduce.case8;

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
 * ----Hadoop 实例8  Join讲解1: 获取员工所在部门信息，输出格式要求：员工编号，员工姓名，部门名称，部门编号
 *
 * 1、原始数据
 * 员工数据
 * empno    ename   job mgr hiredate    sal comm    deptno  loc
 * 7499 allen   salesman    7698    1981-02-20  1600    300 30
 * 7782 clark   managers    7639    1981-06-09  2450        10
 * 7654 martin  salesman    7698    1981-03-20  1250    1400    30  boston
 * 7900 james   clerk   7698    1981-01-09  950     30
 * 7788 scott   analyst 7566    1981-09-01  3000    100 20
 *
 * 部门数据
 * deptno   dname   loc
 * 30   sales   chicago
 * 20   research    dallas
 * 10   accounting  newyork
 *
 * 2、实现的功能类似于
 * select e.empno,e.ename,d.dname,d.deptno from emp e join dept d on e.deptno=d.deptno;
 *
 * key: deptno
 * 第一种思路：(组成一个字符串，_0标识来自员工数据表， _1标识来自部门数据表)
 * Text：empno_ename_0/deptno_dname_1;
 *
 * 第二种思路：（直接构造一个bean，用flag来标识来源）
 * Consume bean: empno/ename/deptno/dname/flag
 *
 *
 * 3、处理join的思路：
 *  将Join key 当作map的输出key, 也就是reduce的输入key ,  这样只要join的key相同，shuffle过后，就会进入到同一个reduce 的key - value list 中去。
 *  需要为join的2张表设计一个通用的一个bean.  并且bean中加一个flag的标志属性，这样可以根据flag来区分是哪张表的数据。
 *  reduce 阶段根据flag来判断是员工数据还是部门数据就很容易了 。而join的真正处理是在reduce阶段。
 */
public class JoinOneJob {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.print("Usage: MaxTemperature<input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = new Job(conf, "join_one_job");
        job.setJarByClass(JoinOneJob.class);

        job.setMapperClass(JoinOneMapper.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Emplyee.class);

        job.setReducerClass(JoinOneReducer.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);

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
