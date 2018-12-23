package org.learning.mapreduce.case3;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class SortMapper extends
        Mapper<LongWritable, Text, IntWritable, Text> {
    private Text val = new Text("");
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        if (line.trim().length() > 0) {
            context.write(new IntWritable(Integer.valueOf(line.trim())), val);
        }
    }
}
