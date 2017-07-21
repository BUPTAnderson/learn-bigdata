package org.learning.jline;

import org.apache.hadoop.hive.ql.exec.FunctionRegistry;
import org.apache.hadoop.hive.ql.parse.HiveParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anderson on 17-7-12.
 */
public class TestJline
{
    public static void main(String[] args)
    {
        List<String> candidateStrings = new ArrayList<String>();
        int i = 1;
        for (String s : FunctionRegistry.getFunctionNames()) {
            if (s.matches("[a-z_]+")) {
                candidateStrings.add(s + "(");
            } else {
                candidateStrings.add(s);
            }
        }
        for (String str : candidateStrings) {
            System.out.println(i++ + "------" + str);
        }

        for (String s : HiveParser.getKeywords()) {
            candidateStrings.add(s);
            System.out.println(i++ + "------" + s);
            candidateStrings.add(s.toLowerCase());
            System.out.println(i++ + "------" + s.toLowerCase());
        }

        System.out.println("--------------");
        System.out.println("+" + spacesForString("default") + "+");
        String[] strs = "show tables;".split(";");
        for (String str : strs) {
            System.out.println("----" + str);
        }
    }

    private static String spacesForString(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        return String.format("%1$-" + s.length() + "s", "");
    }
}
