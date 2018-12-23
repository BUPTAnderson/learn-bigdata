package org.learning.mapreduce.case9;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JoinTwoReducer extends Reducer<IntWritable, User, NullWritable, Text> {
    @Override
    protected void reduce(IntWritable key, Iterable<User> values, Context context) throws IOException, InterruptedException {
        User city = null;
        List<User> list = new ArrayList<User>();

        for (User user : values) {
            if (user.getFlag() == 0) {
                city = new User(user);
            } else {
                list.add(new User(user));
            }
        }

        for (User user : list) {
            user.setCityName(city.getCityName());
            context.write(NullWritable.get(), new Text(user.toString()));
        }
    }
}
