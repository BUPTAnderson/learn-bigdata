package org.learning.mapreduce.case2;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class DeMapper extends
        Mapper<LongWritable, Text, Text, Text> {
    private Text val = new Text("");
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        if (line.trim().length() > 0) {
            context.write(new Text(line), val);
        }
    }
}
