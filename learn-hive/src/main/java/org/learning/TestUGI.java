package org.learning;

import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

/**
 * Created by anderson on 17-7-11.
 */
public class TestUGI
{
    public static void main(String[] args)
            throws IOException
    {
        UserGroupInformation ugi = UserGroupInformation.getCurrentUser();
        String[] list = ugi.getGroupNames();
        for (String str : list) {
            System.out.println(str);
        }
        System.out.println("--" + ugi.getUserName()); // anderson
        System.out.println("++" + ugi.getShortUserName()); // anderson
    }
}
