package org.learning.mapreduce.case6;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Arrays;

public class TopNReducer extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {
    private int len;
    private int[] topN;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        len = context.getConfiguration().getInt("N", 10);
        topN = new int[len];
    }

    @Override
    protected void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        for (IntWritable value : values) {
            add(value.get());
        }
    }

    private void add(int value) {
        if (value > topN[0]) {
            topN[0] = value;
            Arrays.sort(topN);
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        for (int i = len - 1; i >= 0; i--) {
            context.write(new IntWritable(len - i), new IntWritable(topN[i]));
        }
    }
}
