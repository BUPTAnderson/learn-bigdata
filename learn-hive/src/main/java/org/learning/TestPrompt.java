package org.learning;

import org.apache.hadoop.hive.cli.CliSessionState;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.conf.HiveVariableSource;
import org.apache.hadoop.hive.conf.VariableSubstitution;
import org.apache.hadoop.hive.ql.session.SessionState;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anderson on 17-7-12.
 */
public class TestPrompt
{
    public static String chop(String str) {
        if (str == null) {
            return null;
        } else {
            int strLen = str.length();
            if (strLen < 2) {
                return "";
            } else {
                int lastIdx = strLen - 1;
                String ret = str.substring(0, lastIdx);
                char last = str.charAt(lastIdx);
                return last == 10 && ret.charAt(lastIdx - 1) == 13 ? ret.substring(0, lastIdx - 1) : ret;
            }
        }
    }

    private static String[] tokenizeCmd(String cmd) {
        return cmd.split("\\s+");
    }

    public static void main(String[] args)
    {
        CliSessionState ss = new CliSessionState(new HiveConf(SessionState.class));
        HiveConf conf = new HiveConf();
        String prompt = conf.getVar(HiveConf.ConfVars.CLIPROMPT);
        System.out.println("++prompt:" + prompt);
        prompt = prompt + "${A}";
        prompt = new VariableSubstitution(new HiveVariableSource() {
            @Override
            public Map<String, String> getHiveVariable() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("A", "--->");
//                return SessionState.get().getHiveVariables();
                return map;
            }
        }).substitute(conf, prompt);
        System.out.println("++prompt:" + prompt);
        String str = "abc\\;";
        System.out.println(str);
        str = "abc\\";
        System.out.println(str);
        char a = 10;
        System.out.println(a);
        char b = '\n';
        System.out.println((int) b);    // 10
        char c = '\r';
        System.out.println((int) c); // 13
        System.out.println("-->" + chop("a") + "<--");
        System.out.println("-->" + chop("abc") + "<--");
        System.out.println("-->" + chop("abc\r\n") + "<--");
        System.out.println("-->" + chop("abc\r") + "<--");
        System.out.println("-->" + chop("abc\n") + "<--");
        String[] tokens = tokenizeCmd("select *from abc");
        for (String token : tokens) {
            System.out.println(token);
        }
    }
}
