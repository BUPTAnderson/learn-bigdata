package org.learning.mapreduce.case5;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class MaxMinReducer extends
        Reducer<Text, LongWritable, Text, LongWritable> {
    @Override
    protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;

        for (LongWritable value : values) {
            if (value.get() > max) {
                max = value.get();
            }
            if (value.get() < min) {
                min = value.get();
            }
        }

        context.write(new Text("Max"), new LongWritable(max));
        context.write(new Text("Min"), new LongWritable(min));
    }
}
