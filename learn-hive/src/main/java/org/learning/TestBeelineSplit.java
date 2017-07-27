package org.learning;

import jline.console.completer.Completer;
import jline.console.completer.NullCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.hive.beeline.BeeLine;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * Created by anderson on 17-7-26.
 */
public class TestBeelineSplit
{
    public static String[] getConnectionURLExamples() {
        return new String[] {
                "jdbc:JSQLConnect://<hostname>/database=<database>",
                "jdbc:cloudscape:<database>;create=true",
                "jdbc:twtds:sqlserver://<hostname>/<database>",
                "jdbc:daffodilDB_embedded:<database>;create=true",
                "jdbc:datadirect:db2://<hostname>:50000;databaseName=<database>",
                "jdbc:inetdae:<hostname>:1433",
                "jdbc:datadirect:oracle://<hostname>:1521;SID=<database>;MaxPooledStatements=0",
                "jdbc:datadirect:sqlserver://<hostname>:1433;SelectMethod=cursor;DatabaseName=<database>",
                "jdbc:datadirect:sybase://<hostname>:5000",
                "jdbc:db2://<hostname>/<database>",
                "jdbc:hive2://<hostname>",
                "jdbc:hsqldb:<database>",
                "jdbc:idb:<database>.properties",
                "jdbc:informix-sqli://<hostname>:1526/<database>:INFORMIXSERVER=<database>",
                "jdbc:interbase://<hostname>//<database>.gdb",
                "jdbc:microsoft:sqlserver://<hostname>:1433;DatabaseName=<database>;SelectMethod=cursor",
                "jdbc:mysql://<hostname>/<database>?autoReconnect=true",
                "jdbc:oracle:thin:@<hostname>:1521:<database>",
                "jdbc:pointbase:<database>,database.home=<database>,create=true",
                "jdbc:postgresql://<hostname>:5432/<database>",
                "jdbc:postgresql:net//<hostname>/<database>",
                "jdbc:sybase:Tds:<hostname>:4100/<database>?ServiceName=<database>",
                "jdbc:weblogic:mssqlserver4:<database>@<hostname>:1433",
                "jdbc:odbc:<database>",
                "jdbc:sequelink://<hostname>:4003/[Oracle]",
                "jdbc:sequelink://<hostname>:4004/[Informix];Database=<database>",
                "jdbc:sequelink://<hostname>:4005/[Sybase];Database=<database>",
                "jdbc:sequelink://<hostname>:4006/[SQLServer];Database=<database>",
                "jdbc:sequelink://<hostname>:4011/[ODBC MS Access];Database=<database>",
                "jdbc:openlink://<hostname>/DSN=SQLServerDB/UID=sa/PWD=",
                "jdbc:solid://<hostname>:<port>/<UID>/<PWD>",
                "jdbc:dbaw://<hostname>:8889/<database>",
                };
    }

    String[] split(String line) {
        return split(line, " ");
    }

    String[] split(String line, String delim) {
        StringTokenizer tok = new StringTokenizer(line, delim);
        String[] ret = new String[tok.countTokens()];
        int index = 0;
        while (tok.hasMoreTokens()) {
            String t = tok.nextToken();
            t = dequote(t);
            ret[index++] = t;
        }
        return ret;
    }

    String dequote(String str) {
        if (str == null) {
            return null;
        }
        while ((str.startsWith("'") && str.endsWith("'"))
                || (str.startsWith("\"") && str.endsWith("\""))) {
            str = str.substring(1, str.length() - 1);
        }
        return str;
    }

    public static void main(String[] args)
    {
        String command = "connect  jdbc://hive2:192.168.177.77:10000";
        String[] strings = new TestBeelineSplit().split(command);
        for (String str : strings) {
            System.out.println("-->" + str);
        }

        ResourceBundle resourceBundle =
                ResourceBundle.getBundle(BeeLine.class.getSimpleName());
        System.out.println(MessageFormat.format(resourceBundle.getString("help-connect"), new Object[1]));

        List<Completer> c = new LinkedList<Completer>(Arrays.asList(new Completer[] {new StringsCompleter(getConnectionURLExamples())}));
        c.add(new NullCompleter());
        Completer[] parameterCompleters = c.toArray(new Completer[0]);
        System.out.println(parameterCompleters.length);
    }
}
