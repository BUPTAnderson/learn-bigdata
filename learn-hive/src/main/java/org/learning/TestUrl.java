package org.learning;

import org.apache.hive.jdbc.Utils;

/**
 * Created by anderson on 17-7-26.
 */
public class TestUrl
{
    public static void main(String[] args)
    {
        String url = "connect jdbc:hive2://bds-test-001:10000/default;driver=org.apache.hive.jdbc.HiveDriver;user=hadoop;password=123;auth=kerberos";
        String driver = Utils.parsePropertyFromUrl(url, Utils.JdbcConnectionParams.PROPERTY_DRIVER);
        System.out.println(driver);
        String user = Utils.parsePropertyFromUrl(url, Utils.JdbcConnectionParams.AUTH_USER);
        System.out.println(user);
        String password = Utils.parsePropertyFromUrl(url, Utils.JdbcConnectionParams.AUTH_PASSWD);
        System.out.println(password);
        String auth = Utils.parsePropertyFromUrl(url, Utils.JdbcConnectionParams.AUTH_TYPE);
        System.out.println(auth);
    }
}
