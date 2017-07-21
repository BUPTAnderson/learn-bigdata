package org.learning;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main(String[] args)
    {
        String str = "INSERT OVERWRITE TABLE access_log_temp2\n" +
                " SELECT a.user, a.prono, p.maker, p.price\n" +
                " FROM access_log_hbase a JOIN product_hbase p ON (a.prono = p.prono);";
        String[] strs = str.split(";");
        System.out.println(strs.length);
        for (String sql : strs) {
            System.out.println(sql);
        }
    }
}
