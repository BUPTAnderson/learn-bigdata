package org.learning.hdfs;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by anderson on 16-11-22.
 */
public class TestWritableComparable
{
    public static void main(String[] args)
            throws IOException
    {
        Student s = new Student("li", 20, "man");
        FileOutputStream fout = new FileOutputStream(new File("/home/anderson/GitHub/abc.txt"));
        DataOutputStream out = new DataOutputStream(fout);
        s.write(out);
        fout.close();
        out.close();

        //读取对象
        Student s1 = new Student();
        FileInputStream fin = new FileInputStream(new File(""));
        DataInputStream in = new DataInputStream(fin);

        s1.readFields(in);
        System.out.println("name=" + s1.getName() + " age=" + s1.getAge() + " sex=" + s1.getSex());
    }
}

class Student implements WritableComparable<Student>
{
    private Text name;
    private IntWritable age;
    private Text sex;

    public Student()
    {
    }

    public Student(String name, int age, String sex)
    {
        this.name = new Text(name);
        this.age = new IntWritable(age);
        this.sex = new Text(sex);
    }

    public Text getName()
    {
        return name;
    }

    public void setName(Text name)
    {
        this.name = name;
    }

    public IntWritable getAge()
    {
        return age;
    }

    public void setAge(IntWritable age)
    {
        this.age = age;
    }

    public Text getSex()
    {
        return sex;
    }

    public void setSex(Text sex)
    {
        this.sex = sex;
    }

    public int compareTo(Student o)
    {
        int result;
        result = name.compareTo(o.getName());
        if (result != 0) {
            return result;
        }
        result = age.compareTo(o.getAge());
        if (result != 0) {
            return result;
        }
        result = sex.compareTo(o.getSex());
        if (result != 0) {
            return result;
        }
        return 0;
    }

    public void write(DataOutput dataOutput)
            throws IOException
    {
        name.write(dataOutput);
        age.write(dataOutput);
        sex.write(dataOutput);
    }

    public void readFields(DataInput dataInput)
            throws IOException
    {
        name.readFields(dataInput);
        age.readFields(dataInput);
        sex.readFields(dataInput);
    }
}