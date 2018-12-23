package org.learning.mapreduce.case3;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class SortReducer extends
        Reducer<IntWritable, Text, IntWritable, IntWritable> {
    private IntWritable num = new IntWritable(1);

    @Override
    protected void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        for (Text value : values) {
            context.write(num, key);
            num = new IntWritable(num.get() + 1);
        }
    }
}
