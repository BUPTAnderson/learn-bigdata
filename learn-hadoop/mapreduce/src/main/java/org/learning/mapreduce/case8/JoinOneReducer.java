package org.learning.mapreduce.case8;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JoinOneReducer extends Reducer<IntWritable, Emplyee, NullWritable, Text> {
    @Override
    protected void reduce(IntWritable key, Iterable<Emplyee> values, Context context) throws IOException, InterruptedException {
        Emplyee dept = null;
        List<Emplyee> list = new ArrayList<>();
        for (Emplyee e : values) {
            if (e.getFlag() == 0) {
                dept = new Emplyee(e);
            } else {
                list.add(new Emplyee(e));
            }
        }

        for (Emplyee e : list) {
            e.setDname(dept.getDname());
            context.write(NullWritable.get(), new Text(e.toString()));
        }
    }
}
