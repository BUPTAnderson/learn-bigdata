package org.learning;

import org.apache.hadoop.hive.conf.HiveConf;

import java.io.File;
import java.util.Map;

/**
 * Created by anderson on 17-7-28.
 */
public class TestConf2
{
    public static void main(String[] args)
    {
        HiveConf hiveConf = new HiveConf();
        hiveConf.set("aaa", "123");
        System.out.println(hiveConf.get("aaa"));
        HiveConf hiveConf1 = new HiveConf(hiveConf);
        hiveConf1.set("aaa", "222");
        System.out.println(hiveConf.get("aaa"));
        System.out.println(hiveConf1.get("aaa"));

        Map<String, String> map = HiveConf.getConfSystemProperties();
        System.out.println(map.size());
        while (map.entrySet().iterator().hasNext()) {
            Map.Entry<String, String> entry = map.entrySet().iterator().next();
            System.out.println("key=" + entry.getKey() + ", value=" + entry.getValue());
        }

        String resourceDirPath = "/tmp" + File.separator + "98f4a59c-2eab-4918-a0dd-3018510700ed_resources";
        System.out.println(resourceDirPath);
        File resourceDir = new File(resourceDirPath);
        boolean doesExist = resourceDir.exists();
        System.out.println(doesExist);
        if (doesExist && !resourceDir.isDirectory()) {
            throw new RuntimeException(resourceDir + " is not a directory");
        }
        if (!doesExist && !resourceDir.mkdirs()) {
            throw new RuntimeException("Couldn't create directory " + resourceDir);
        }
    }
}
