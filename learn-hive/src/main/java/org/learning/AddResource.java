package org.learning;

import org.apache.hadoop.hive.ql.session.SessionState;

import java.util.Arrays;
import java.util.List;

/**
 * Created by anderson on 17-7-14.
 */
public class AddResource
{
    public String[] tokenizeCmd(String cmd) {
        return cmd.split("\\s+");
    }

    public String getFirstCmd(String cmd, int length) {
        return cmd.substring(length).trim();
    }

    public static void main(String[] args)
    {
        String cmd = "add jar /abc.jar";
        AddResource addResource = new AddResource();
        String firstToken = addResource.tokenizeCmd(cmd.trim())[0];
        String cmd1 = addResource.getFirstCmd(cmd.trim(), firstToken.length());
        System.out.println(cmd1);
        SessionState.ResourceType resourceType = SessionState.find_resource_type(cmd1.split("\\s+")[0]);
        System.out.println(resourceType.name());
        String command = "jar /abc.jar /cde.jar";
        String[] tokens = command.split("\\s+");
        List<String> list = Arrays.asList(Arrays.copyOfRange(tokens, 1, tokens.length));
        for (String str : list) {
            System.out.println(str);
        }
    }
}
