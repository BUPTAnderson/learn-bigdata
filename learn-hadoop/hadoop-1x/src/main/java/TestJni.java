/**
 * 1. 编译当前文件：
 * cd ~/IdeaProjects/learn-bigdata/learn-hadoop/hadoop-1x/src/main/java/
 * javac TestJni.java
 * 2. 生成头文件
 * javah -jni TestJni
 * 执行后发现生成文件TestJni.h
 * 3. mc TestJni.h MyJni.h
 * 3. touch MyJni.c
 * 4. 将TestJni.h内容拷贝到MyJni.c中， 继续编辑MyJni.c
 * 5. gcc -fPIC -shared -o libMyJni.so MyJni.c -I/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/include -I/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/include/darwin -I.
 *  linux下该条命令是：gcc -fPIC -shared -o libMyJni.so MyJni.c -I/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/include -I/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/include/linux -I.
 *  执行完后生成：libMyJni.so
 *  7. 执行： java -Djava.library.path=. TestJni
 */

public class TestJni {
    public native void print(String str);
    static {
        System.out.println("MyJni");
    }
    public static void main(String[] args)
    {
        new TestJni().print("hello Jni");
    }
}
