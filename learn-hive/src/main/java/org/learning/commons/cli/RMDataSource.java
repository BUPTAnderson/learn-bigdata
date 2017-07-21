package org.learning.commons.cli;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by anderson on 17-7-11.
 * 链接: https://www.ibm.com/developerworks/cn/java/j-lo-commonscli/
 */
public class RMDataSource
{
    /**
     * @param args 输入参数
     */
    public static void main(String[] args) {
        simpleTest(args);
    }

    public static void simpleTest(String[] args) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("message",
                Locale.getDefault());
        Options opts = new Options();
        opts.addOption("h", false, resourceBundle.getString("HELP_DESCRIPTION"));
        opts.addOption("i", true, resourceBundle.getString("HELP_IPADDRESS"));
        opts.addOption("p", true, resourceBundle.getString("HELP_PORT"));
        opts.addOption("t", true, resourceBundle.getString("HELP_PROTOCOL"));
        BasicParser parser = new BasicParser();
        CommandLine cl;
        try {
            cl = parser.parse(opts, args);
            if (cl.getOptions().length > 0) {
                if (cl.hasOption('h')) {
                    HelpFormatter hf = new HelpFormatter();
                    hf.printHelp("Options", opts);
                } else {
                    String ip = cl.getOptionValue("i");
                    String port = cl.getOptionValue("p");
                    String protocol = cl.getOptionValue("t");
//                    if(!CIMServiceFactory.getinstance().isIPValid(ip))
                    if (true) {
                        System.err.println(resourceBundle.getString("INVALID_IP"));
                        System.exit(1);
                    }
                    try {
//                        int rc = CIMServiceFactory.getinstance().rmdatasource(
//                                ip, port, protocol);
                        int rc = 0;
                        if (rc == 0) {
                            System.out.println(resourceBundle
                                    .getString("RMDATASOURCE_SUCCEEDED"));
                        } else {
                            System.err.println(resourceBundle
                                    .getString("RMDATASOURCE_FAILED"));
                        }
                    } catch (Exception e) {
                        System.err.println(resourceBundle
                                .getString("RMDATASOURCE_FAILED"));
                        e.printStackTrace();
                    }
                }
            } else {
                System.err.println(resourceBundle.getString("ERROR_NOARGS"));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
