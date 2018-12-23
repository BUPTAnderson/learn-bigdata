package org.learning.mapreduce.case4;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.StringTokenizer;

public class AvgMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        StringTokenizer tokenizer = new StringTokenizer(value.toString());
        if (tokenizer.countTokens() == 2) {
            context.write(new Text(tokenizer.nextToken().trim()), new IntWritable(Integer.parseInt(tokenizer.nextToken())));
        }
    }

    public static void main(String[] args) {
        StringTokenizer tokenizer = new StringTokenizer("张三    88");
        System.out.println(tokenizer.countTokens());
        System.out.println(tokenizer.nextToken() + "," + tokenizer.nextToken());
    }
}
