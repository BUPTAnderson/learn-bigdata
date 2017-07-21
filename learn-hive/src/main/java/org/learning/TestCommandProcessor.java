package org.learning;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.processors.CommandProcessor;
import org.apache.hadoop.hive.ql.processors.CommandProcessorFactory;
import org.apache.hadoop.hive.ql.processors.HiveCommand;

import java.sql.SQLException;

/**
 * Created by anderson on 17-7-13.
 */
public class TestCommandProcessor
{
    private static String[] tokenizeCmd(String cmd) {
        return cmd.split("\\s+");
    }

    public static void main(String[] args)
            throws SQLException
    {
//        String cmd_trimmed = "select * from abc";
//        cmd_trimmed = "create table abc";
//        cmd_trimmed = "dfs -ls";
//        cmd_trimmed = "set a=b";
//        String[] tokens = tokenizeCmd(cmd_trimmed);
//        HiveConf conf = new HiveConf();
//        CommandProcessor proc = CommandProcessorFactory.get(tokens, (HiveConf) conf);
//        System.out.println(proc.getClass().getName());
        HiveCommand command = HiveCommand.find(tokenizeCmd("create table abc"), false);
        System.out.println(command == null);
        command = HiveCommand.find(tokenizeCmd("set a=b"), false);
        System.out.println(command.name());
        command = HiveCommand.find(tokenizeCmd("set role admin"), false);
        System.out.println(command == null);
        command = HiveCommand.find(tokenizeCmd("delete from abc"), false);
        System.out.println(command == null);
        command = HiveCommand.find(tokenizeCmd("select * from abc"), false);
        System.out.println(command == null);
        command = HiveCommand.find(tokenizeCmd("add jar /abc.jar"), false);
        System.out.println(command == null);
        String[] tokens = tokenizeCmd("add jar /abc.jar");
        HiveConf conf = new HiveConf();
        CommandProcessor processor = CommandProcessorFactory.getForHiveCommandInternal(tokens, conf, false);
        if (processor != null) {
            System.out.println(processor.getClass().getName());
        }
    }
}
