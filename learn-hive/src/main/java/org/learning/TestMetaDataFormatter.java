package org.learning;

import org.apache.hadoop.hive.conf.HiveConf;

/**
 * Created by anderson on 17-7-17.
 */
public class TestMetaDataFormatter
{
    public static void main(String[] args)
    {
        HiveConf conf = new HiveConf();
        String format = conf.get(HiveConf.ConfVars.HIVE_DDL_OUTPUT_FORMAT.varname, "text");
        System.out.println(format);
    }
}
