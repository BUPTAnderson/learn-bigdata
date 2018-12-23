package org.learning.mapreduce.case6;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Arrays;

public class TopNMapper extends
        Mapper<LongWritable, Text, IntWritable, IntWritable> {
    private int len;
    private int[] topN;
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        len = context.getConfiguration().getInt("N", 10);
        topN = new int[len];
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString().trim();
        if (line.length() > 0) {
            String[] arr = line.split(",");
            if (arr.length == 4) {
                int payment = Integer.parseInt(arr[2]);
                add(payment);
            }
        }
    }

    private void add(int value) {
        if (topN[0] < value) {
            topN[0] = value;
            Arrays.sort(topN);
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        for (int x : topN) {
            context.write(new IntWritable(x), new IntWritable(x));
        }
    }
}
