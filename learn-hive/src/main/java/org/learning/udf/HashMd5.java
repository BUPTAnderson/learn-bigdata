package org.learning.udf;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.MD5Hash;
import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * Created by anderson on 17-10-13.
 */
public class HashMd5 extends UDF
{
    public String evaluate(String cookie) {
        return MD5Hash.getMD5AsHex(Bytes.toBytes(cookie));
    }
}
