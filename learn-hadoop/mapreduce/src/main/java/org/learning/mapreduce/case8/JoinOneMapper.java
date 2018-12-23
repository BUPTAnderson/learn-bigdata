package org.learning.mapreduce.case8;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class JoinOneMapper extends Mapper<LongWritable, Text, IntWritable, Emplyee> {
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        String[] arr = line.split("\t");
        if (arr.length <= 3) {
            Emplyee emplyee = new Emplyee();
            emplyee.setDeptno(arr[0]);
            emplyee.setDname(arr[1]);
            emplyee.setFlag(0);

            context.write(new IntWritable(Integer.parseInt(arr[0])), emplyee);
        } else {
            Emplyee emplyee = new Emplyee();
            emplyee.setEmpno(arr[0]);
            emplyee.setEname(arr[1]);
            emplyee.setDeptno(arr[7]);
            emplyee.setFlag(1);

            context.write(new IntWritable(Integer.parseInt(arr[7])), emplyee);
        }
    }
}
