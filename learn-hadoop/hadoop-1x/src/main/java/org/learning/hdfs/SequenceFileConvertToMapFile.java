package org.learning.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile;

import java.net.URI;

/**
 * Created by anderson on 17-2-24.
 */
public class SequenceFileConvertToMapFile
{
    public static final String uri = "hdfs://anderson-JD:9111";
    public static final String[] data = {
            "one, two, buckle my shoe",
            "three, four, shut the door",
            "five, six, pick up sticks",
            "seven, eight, lay them straight",
            "nine, ten, a big fat hen"
    };

    public void testMapFix(String sqFile) throws Exception
    {
        String uri = sqFile;
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(uri), conf);
        Path path = new Path(uri);
        Path mapData = new Path(path, MapFile.DATA_FILE_NAME);
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, mapData, conf);
        Class keyClass = reader.getKeyClass();
        Class valueClass = reader.getValueClass();
        reader.close();
        long entries = MapFile.fix(fs, path, keyClass, valueClass, false, conf);
        System.out.printf("create MapFile from sequenceFile about %d entries!", entries);
    }

    public static void main(String[] args)
            throws Exception
    {
        SequenceFileConvertToMapFile fixer = new SequenceFileConvertToMapFile();
        fixer.testMapFix("/io/tmp1.map");
    }
}
