package org.learning.commons.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.hadoop.hive.cli.CliSessionState;
import org.apache.hadoop.hive.common.cli.CommonCliOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by anderson on 17-7-11.
 */
public class TestHiveCommond
{
    private final Options options = new Options();
    private org.apache.commons.cli.CommandLine commandLine;
    Map<String, String> hiveVariables = new HashMap<String, String>();

    @SuppressWarnings("static-access")
    public TestHiveCommond() {
        // -database database
        options.addOption(OptionBuilder
                .hasArg()
                .withArgName("databasename")
                .withLongOpt("database")
                .withDescription("Specify the database to use")
                .create());

        // -e 'quoted-query-string'
        options.addOption(OptionBuilder
                .hasArg()
                .withArgName("quoted-query-string")
                .withDescription("SQL from command line")
                .create('e'));

        // -f <query-file>
        options.addOption(OptionBuilder
                .hasArg()
                .withArgName("filename")
                .withDescription("SQL from files")
                .create('f'));

        // -i <init-query-file>
        options.addOption(OptionBuilder
                .hasArg()
                .withArgName("filename")
                .withDescription("Initialization SQL file")
                .create('i'));

        // -hiveconf x=y
        options.addOption(OptionBuilder
                .withValueSeparator()
                .hasArgs(2)
                .withArgName("property=value")
                .withLongOpt("hiveconf")
                .withDescription("Use value for given property")
                .create());

        // Substitution option -d, --define
        options.addOption(OptionBuilder
                .withValueSeparator()
                .hasArgs(2)
                .withArgName("key=value")
                .withLongOpt("define")
                .withDescription("Variable subsitution to apply to hive commands. e.g. -d A=B or --define A=B")
                .create('d'));

        // Substitution option --hivevar
        options.addOption(OptionBuilder
                .withValueSeparator()
                .hasArgs(2)
                .withArgName("key=value")
                .withLongOpt("hivevar")
                .withDescription("Variable subsitution to apply to hive commands. e.g. --hivevar A=B")
                .create());

        // [-S|--silent]
        options.addOption(new Option("S", "silent", false, "Silent mode in interactive shell"));

        // [-v|--verbose]
        options.addOption(new Option("v", "verbose", false, "Verbose mode (echo executed SQL to the console)"));

        // [-H|--help]
        options.addOption(new Option("H", "help", false, "Print help information"));
    }

    public boolean process_stage1(String[] argv) {
        try {
            commandLine = new GnuParser().parse(options, argv);
            Properties confProps = commandLine.getOptionProperties("hiveconf");
            for (String propKey : confProps.stringPropertyNames()) {
                // with HIVE-11304, hive.root.logger cannot have both logger name and log level.
                // if we still see it, split logger and level separately for hive.root.logger
                // and hive.log.level respectively
                if (propKey.equalsIgnoreCase("hive.root.logger")) {
                    CommonCliOptions.splitAndSetLogger(propKey, confProps);
                } else {
                    System.setProperty(propKey, confProps.getProperty(propKey));
                }
            }

            Properties hiveVars = commandLine.getOptionProperties("define");
            for (String propKey : hiveVars.stringPropertyNames()) {
                hiveVariables.put(propKey, hiveVars.getProperty(propKey));
            }

            Properties hiveVars2 = commandLine.getOptionProperties("hivevar");
            for (String propKey : hiveVars2.stringPropertyNames()) {
                hiveVariables.put(propKey, hiveVars2.getProperty(propKey));
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            printUsage();
            return false;
        }
        return true;
    }

    public boolean process_stage2(CliSessionState ss) {
        ss.getConf();

        if (commandLine.hasOption('H')) {
            printUsage();
            return false;
        }

        ss.setIsSilent(commandLine.hasOption('S'));

        ss.database = commandLine.getOptionValue("database");

        ss.execString = commandLine.getOptionValue('e');

        ss.fileName = commandLine.getOptionValue('f');

        ss.setIsVerbose(commandLine.hasOption('v'));

        String[] initFiles = commandLine.getOptionValues('i');
        if (null != initFiles) {
            ss.initFiles = Arrays.asList(initFiles);
        }

        if (ss.execString != null && ss.fileName != null) {
            System.err.println("The '-e' and '-f' options cannot be specified simultaneously");
            printUsage();
            return false;
        }

        if (commandLine.hasOption("hiveconf")) {
            Properties confProps = commandLine.getOptionProperties("hiveconf");
            for (String propKey : confProps.stringPropertyNames()) {
                ss.cmdProperties.setProperty(propKey, confProps.getProperty(propKey));
            }
        }

        return true;
    }

    private void printUsage() {
        new HelpFormatter().printHelp("hive", options);
    }

    public Map<String, String> getHiveVariables() {
        return hiveVariables;
    }

    public Options getOptions()
    {
        return options;
    }

    public CommandLine getCommandLine()
    {
        return commandLine;
    }

    public void setCommandLine(CommandLine commandLine)
    {
        this.commandLine = commandLine;
    }

    public void setHiveVariables(Map<String, String> hiveVariables)
    {
        this.hiveVariables = hiveVariables;
    }

    public static void main(String[] args)
            throws ParseException
    {
        TestHiveCommond oproc = new TestHiveCommond();

        String[] argv = new String[2];
        argv[0] = "--database"; // 设置的是LongOpt, 所以使用--database和-database都可以
        argv[1] = "default";
        Parser parser = new GnuParser();
        CommandLine commandLine = parser.parse(oproc.getOptions(), argv);
        String value = commandLine.getOptionValue("database");
        System.out.println(value);
        argv = new String[4];
        argv[0] = "-d"; // 使用--define, -define, -d都可以
        argv[1] = "a=b";
        argv[2] = "-d";
        argv[3] = "x=y";
        commandLine = parser.parse(oproc.getOptions(), argv);
        Properties confProps = commandLine.getOptionProperties("d"); // 使用d或define都可以
        for (String propKey : confProps.stringPropertyNames()) {
            System.out.println("key:" + propKey + ", value:" + confProps.getProperty(propKey));
        }
    }
}
