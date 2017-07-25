package org.learning;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName)
    {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        String str = "hdfs://ns1/user/hive2/9590c290c6cc6a0a46e3da7a591aed64/default/ysm_test.db";
        String[] strs = str.split("/");
        String[] dbs = strs[strs.length - 1].split("\\.");
        System.out.println(strs.length);
        System.out.println(dbs.length);
        String db = strs[strs.length - 2] + "/" + dbs[0];
        System.out.println(strs[strs.length - 1].contains("."));
        System.out.println(db);
    }
}
