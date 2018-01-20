package org.learning.udf;

import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

/**
 * 一般运行包含manifest的jar包，可以使用

 * java -jar <jar-file-name>.jar
 * 如果jar里没有 manifest，则可以使用

 * java -cp foo.jar full.package.name.ClassName 或者: java -classpath foo.jar full.package.name.ClassName
 * 当main类依赖多个jar时，可以把多个jar打包到一个目录，然后用-Djava.ext.dirs指定该目录，引用依赖的多个jar。

 * java -Djava.ext.dirs=<多个jar包的目录> com.test.HelloWordMain
 * 如果用-cp则需要写每一个jar，很麻烦
 */

/**
 * 编译时指定依赖的jar包
 * javac -classpath /home/hadoop/redistest/jedis-2.8.2.jar RedisJava.java
 * 执行时指定依赖jar所在目录 java -Djava.ext.dirs=/home/hadoop/redistest -cp . RedisJava

 * Created by anderson on 17-10-13.
 * 编写文件:
 * vim Lower.java
 * 编译:
 * javac -classpath /export/App/apache-hive-1.2.1-bin/lib/hive-exec-1.2.1.jar:/export/App/hadoop-2.6.1/share/hadoop/common/hadoop-common-2.6.1.jar Lower.java
 * 编译成功后会看到 Lower.class文件
 * 创建jar包: jar -cf lower.jar Lower.class
 * 创建成功后会看到 lower.jar文件
 * 进入hive:
 * 添加jar包:
 * beeline> add jar /home/hadoop/lower.jar
 * 创建udf函数:
 * create temporary function my_lower as 'Lower'; // 有包名的话指定包名: create temporary function my_lower as 'org.learning.udf.Lower';
 * 使用函数my_lower:
 * select my_lower(name) from idname;
 */
public class Lower extends UDF
{
    public Text evaluate(final Text s) {
        if (s == null) {
            return null;
        }
        return new Text(s.toString().toLowerCase());
    }
}
