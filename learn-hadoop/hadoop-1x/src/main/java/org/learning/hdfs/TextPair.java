package org.learning.hdfs;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

/**
 * 为了演示如何创建一个自定义Writable，编写一个表示一对字符串的实现，名为TextPair
 *
 * Created by anderson on 17-2-24.
 */
public class TextPair
        implements WritableComparable<TextPair>
{
    private Text first;
    private Text second;

    public TextPair()
    {
        first = new Text();
        second = new Text();
    }

    public TextPair(Text first, Text second)
    {
        this.first = first;
        this.second = second;
    }

    public TextPair(String first, String second)
    {
        this.first = new Text(first);
        this.second = new Text(second);
    }

    public Text getFirst()
    {
        return first;
    }

    public Text getSecond()
    {
        return second;
    }

    @Override
    public int compareTo(TextPair tp)
    {
        int cmp = first.compareTo(tp.first);
        if (cmp != 0) {
            return cmp;
        }
        return second.compareTo(tp.second);
    }

    @Override
    public void write(DataOutput out)
            throws IOException
    {
        first.write(out);
        second.write(out);
    }

    @Override
    public void readFields(DataInput in)
            throws IOException
    {
        first.readFields(in);
        second.readFields(in);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof TextPair) {
            TextPair tp = (TextPair) o;
            return first.equals(tp.first) && second.equals(tp.second);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(first, second);
    }

    // 参考IntWritable的实现
    /** A Comparator optimized for TextPair. */
    public static class Comparator extends WritableComparator {
        public Comparator()
        {
            super(TextPair.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2)
        {
            try {
                int firstL1 = WritableUtils.decodeVIntSize(b1[s1]) + readVInt(b1, s1);
                int firstL2 = WritableUtils.decodeVIntSize(b2[s2]) + readVInt(b2, s2);
                int cmp = WritableComparator.get(Text.class).compare(b1, s1, firstL1, b2, s2, firstL2);
                if (cmp != 0) {
                    return cmp;
                }
                return WritableComparator.get(Text.class).compare(b1, s1 + firstL1, l1 - firstL1, b2, s2 + firstL2, l2 - firstL2);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    // 静态代码块注册原始的comparator以便MapReduce每次看到TextPair类，就知道使用 new Comparator（即上面定义的Comparator）作为默认的comparator
    static {
        WritableComparator.define(TextPair.class, new Comparator());
    }
}
