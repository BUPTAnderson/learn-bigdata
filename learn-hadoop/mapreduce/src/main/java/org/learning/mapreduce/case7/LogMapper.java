package org.learning.mapreduce.case7;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class LogMapper extends
        Mapper<LongWritable, Text, Text, IntWritable> {
    private IntWritable constValue = new IntWritable(1);
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString().trim();
        String tmp = handlerLog(line);
        context.write(new Text(tmp), constValue);
    }

    private String handlerLog(String line) {
        String result = "";
        if (line.length() > 20) {
            if (line.indexOf("GET") > 0) {
                result = line.substring(line.indexOf("GET"), line.indexOf("HTTP/1.0")).trim();
            } else if (line.indexOf("POST") > 0) {
                result = line.substring(line.indexOf("POST"), line.indexOf("HTTP/1.0")).trim();
            }
        }

        return result;
    }
}
