package org.learning.hdfs;

/**
 * Created by anderson on 16-11-19.
 */
public class TestHDFS
{
    // 参考测试类
    // com.bupt.test.hdfs.TestHDFS
}

class MyJni
{
    public native void print(String str);
    static {
        System.out.println("MyJni");
    }
    public static void main(String[] args)
    {
        new MyJni().print("hello Jni");
    }
}
