package org.learning.mapreduce.case5;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class MaxMinMapper extends
        Mapper<LongWritable, Text, Text, LongWritable> {
    private Text keyText = new Text("Key");

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        if (line.trim().length() > 0) {
            context.write(keyText, new LongWritable(Long.parseLong(line.trim())));
        }
    }
}
