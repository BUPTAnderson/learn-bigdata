package org.learning.udf;

import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

/**
 * Created by anderson on 17-10-13.
 * 编写文件:
 * vim Lower.java
 * 编译:
 * jvac -classpath /export/App/apache-hive-1.2.1-bin/lib/hive-exec-1.2.1.jar:/export/App/hadoop-2.6.1/share/hadoop/common/hadoop-common-2.6.1.jar Lower.java
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
