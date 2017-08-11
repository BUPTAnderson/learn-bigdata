package org.learning;

import jline.console.ConsoleReader;

import java.io.IOException;

/**
 * Created by anderson on 17-7-31.
 */
public class TestHandleMultiLineCmd
{
    public String handleMultiLineCmd(String line) throws IOException
    {
        //When using -e, console reader is not initialized and command is a single line
        while (!(line.trim().endsWith(";"))) {
            StringBuilder prompt = new StringBuilder("0: jdbc:hive2://192.168.177.80:10000/default>");
            if (!false) {
                for (int i = 0; i < prompt.length() - 1; i++) {
                    if (prompt.charAt(i) != '>') {
                        prompt.setCharAt(i, i % 2 == 0 ? '.' : ' ');
                    }
                }
            }

            String extra = new ConsoleReader().readLine(prompt.toString());

            if (extra == null) { //it happens when using -f and the line of cmds does not end with ;
                break;
            }
            line += "\n" + extra;
        }
        return line;
    }

    public static void main(String[] args)
            throws IOException
    {
//        TestHandleMultiLineCmd multiLineCmd = new TestHandleMultiLineCmd();
//        String str = multiLineCmd.handleMultiLineCmd("show");
//        System.out.println(str);

        String line = "select * from abc where a like '\\;";
        System.out.println(line);
        for (String cmdpart : line.split(";")) {
            if (cmdpart.endsWith("\\")) {
                System.out.println(cmdpart.substring(0, cmdpart.length() - 1));
            }
        }
    }
}
