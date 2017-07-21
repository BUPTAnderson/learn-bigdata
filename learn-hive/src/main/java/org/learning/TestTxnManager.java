package org.learning;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.lockmgr.HiveTxnManager;
import org.apache.hadoop.hive.ql.lockmgr.LockException;
import org.apache.hadoop.hive.ql.lockmgr.TxnManagerFactory;

/**
 * Created by anderson on 17-7-18.
 */
public class TestTxnManager
{
    public static void main(String[] args)
            throws LockException
    {
        HiveConf conf = new HiveConf();
        HiveTxnManager manager = TxnManagerFactory.getTxnManagerFactory().getTxnManager(conf);
        System.out.println(manager.getClass().getName());
    }
}
