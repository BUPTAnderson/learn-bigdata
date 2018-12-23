package org.learning.mapreduce.case1;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class NewMaxTemperatureMapper extends
        Mapper<LongWritable, Text, Text, IntWritable> {
    private static final int ERRORDATA = 9999;
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        String year = line.substring(15, 19);
        int airTemperature;
        if (line.charAt(45) == '+') {
            airTemperature = Integer.parseInt(line.substring(46, 50));
        } else {
            airTemperature = Integer.parseInt(line.substring(45, 50));
        }

        String quality = line.substring(50, 51);
        if (airTemperature != ERRORDATA && quality.matches("[01459]")) {
            context.write(new Text(year), new IntWritable(airTemperature));
        }
    }

    public static void main(String[] args) {
        String line = "0067011990999991950051507004888888889999999N9+99991+9999999999999999999999";
        String year = line.substring(15, 19);
        int airTemperature;
        if (line.charAt(45) == '+') {
            airTemperature = Integer.parseInt(line.substring(46, 50));
        } else {
            airTemperature = Integer.parseInt(line.substring(45, 50));
        }

        String quality = line.substring(50, 51);
        if (airTemperature != ERRORDATA && quality.matches("[01459]")) {
            System.out.println("year:" + year + ", temperature: " + airTemperature);
        }
    }
}
