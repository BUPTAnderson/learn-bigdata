package org.learning;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.common.LogUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

/**
 * Created by anderson on 17-7-13.
 */
public class TestLog
{
    private static final String HIVE_L4J = "hive-log4j2.properties";
    private static final String HIVE_EXEC_L4J = "hive-exec-log4j2.properties";
    private static final Logger l4j = LoggerFactory.getLogger(LogUtils.class);

    public static String initHiveLog4jCommon(HiveConf conf, HiveConf.ConfVars confVarName)
            throws LogUtils.LogInitializationException
    {
        if (HiveConf.getVar(conf, confVarName).equals("")) {
            // if log4j configuration file not set, or could not found, use default setting
            return initHiveLog4jDefault(conf, "", confVarName);
        } else {
            // if log4j configuration file found successfully, use HiveConf property value
            String log4jFileName = HiveConf.getVar(conf, confVarName);
            File log4jConfigFile = new File(log4jFileName);
            boolean fileExists = log4jConfigFile.exists();
            if (!fileExists) {
                // if property specified file not found in local file system
                // use default setting
                return initHiveLog4jDefault(
                        conf, "Not able to find conf file: " + log4jConfigFile, confVarName);
            } else {
                // property speficied file found in local file system
                // use the specified file
                if (confVarName == HiveConf.ConfVars.HIVE_EXEC_LOG4J_FILE) {
                    String queryId = HiveConf.getVar(conf, HiveConf.ConfVars.HIVEQUERYID);
                    if (queryId == null || queryId.trim().isEmpty()) {
                        queryId = "unknown-" + System.currentTimeMillis();
                    }
                    System.setProperty(HiveConf.ConfVars.HIVEQUERYID.toString(), queryId);
                }
                final boolean async = checkAndSetAsyncLogging(conf);
                Configurator.initialize(null, log4jFileName);
                logConfigLocation(conf);
                return "Logging initialized using configuration in " + log4jConfigFile + " Async: " + async;
            }
        }
    }

    private static String initHiveLog4jDefault(
            HiveConf conf, String logMessage, HiveConf.ConfVars confVarName)
            throws LogUtils.LogInitializationException
    {
        URL hiveL4J = null;
        switch (confVarName) {
            case HIVE_EXEC_LOG4J_FILE:
                hiveL4J = LogUtils.class.getClassLoader().getResource(HIVE_EXEC_L4J);
                if (hiveL4J == null) {
                    hiveL4J = LogUtils.class.getClassLoader().getResource(HIVE_L4J);
                }
                System.setProperty(HiveConf.ConfVars.HIVEQUERYID.toString(),
                        HiveConf.getVar(conf, HiveConf.ConfVars.HIVEQUERYID));
                break;
            case HIVE_LOG4J_FILE:
                hiveL4J = LogUtils.class.getClassLoader().getResource(HIVE_L4J);
                break;
            default:
                break;
        }
        if (hiveL4J != null) {
            final boolean async = checkAndSetAsyncLogging(conf);
            Configurator.initialize(null, hiveL4J.toString());
            logConfigLocation(conf);
            return (logMessage + "\n" + "Logging initialized using configuration in " + hiveL4J +
                    " Async: " + async);
        } else {
            throw new LogUtils.LogInitializationException(
                    logMessage + "Unable to initialize logging using "
                            + HIVE_L4J + ", not found on CLASSPATH!");
        }
    }

    private static void logConfigLocation(HiveConf conf) throws LogUtils.LogInitializationException
    {
        // Log a warning if hive-default.xml is found on the classpath
        if (conf.getHiveDefaultLocation() != null) {
            l4j.warn("DEPRECATED: Ignoring hive-default.xml found on the CLASSPATH at "
                    + conf.getHiveDefaultLocation().getPath());
        }
        // Look for hive-site.xml on the CLASSPATH and log its location if found.
        if (conf.getHiveSiteLocation() == null) {
            l4j.warn("hive-site.xml not found on CLASSPATH");
        } else {
            l4j.debug("Using hive-site.xml found on CLASSPATH at "
                    + conf.getHiveSiteLocation().getPath());
        }
    }

    public static boolean checkAndSetAsyncLogging(final Configuration conf) {
        final boolean asyncLogging = HiveConf.getBoolVar(conf, HiveConf.ConfVars.HIVE_ASYNC_LOG_ENABLED);
        if (asyncLogging) {
            System.setProperty("Log4jContextSelector",
                    "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
            // default is ClassLoaderContextSelector which is created during automatic logging
            // initialization in a static initialization block.
            // Changing ContextSelector at runtime requires creating new context factory which will
            // internally create new context selector based on system property.
            LogManager.setFactory(new Log4jContextFactory());
        }
        return asyncLogging;
    }

    private static URL checkConfigFile(File f) {
        try {
            return (f.exists() && f.isFile()) ? f.toURI().toURL() : null;
        } catch (Throwable e) {
            if (l4j.isInfoEnabled()) {
                l4j.info("Error looking for config " + f, e);
            }
            System.err.println("Error looking for config " + f + ": " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args)
            throws LogUtils.LogInitializationException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = HiveConf.class.getClassLoader();
        }

        String confPath = System.getenv("HIVE_CONF_DIR");
        URL result = checkConfigFile(new File(confPath, "hive-site.xml"));
        System.out.println(result.toString());
//        URL hiveSiteURL = classLoader.getResource("hive-site.xml");
//        String pathSeparator = File.pathSeparator;
//        System.out.println(pathSeparator);
//        System.out.println(File.separator);
//        String homePath = System.getenv("HIVE_HOME");
//        System.out.println(homePath);
//        String javaHome = System.getenv("JAVA_HOME");
//        System.out.println(javaHome);
//        System.out.println(System.getenv("USER"));
//        HiveConf conf = new HiveConf();
//        String str = HiveConf.getVar(conf, HiveConf.ConfVars.HIVE_EXEC_LOG4J_FILE);
//        HiveConf.ConfVars confVars = HiveConf.ConfVars.HIVE_LOG4J_FILE;
//        String str2 = initHiveLog4jCommon(conf, confVars);
//        System.out.println(str2);\
        String homePath = System.getenv("HIVE_HOME");
        String nameInConf = "conf" + File.separator + "hive-site.xml";
        System.out.println(nameInConf);
        result = checkConfigFile(new File(homePath, nameInConf));
        if (result == null) {
            System.out.println("result is null");
        } else {
            System.out.println(result.toString());
        }
    }
}
