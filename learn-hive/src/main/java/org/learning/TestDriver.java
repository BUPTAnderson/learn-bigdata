package org.learning;

import org.apache.hive.beeline.ClassNameCompleter;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by anderson on 17-7-26.
 */
public class TestDriver
{
    static final String PASSWD_MASK = "[passwd stripped]";

    static final SortedSet<String> KNOWN_DRIVERS = new TreeSet<String>(Arrays.asList(
            new String[] {
                    "org.apache.hive.jdbc.HiveDriver",
                    "org.apache.hadoop.hive.jdbc.HiveDriver",
                    }));

    Driver[] scanDrivers(boolean knownOnly) throws IOException
    {
        long start = System.currentTimeMillis();

        Set<String> classNames = new HashSet<String>();

        if (!knownOnly) {
            classNames.addAll(Arrays.asList(
                    ClassNameCompleter.getClassNames()));
        }

        classNames.addAll(KNOWN_DRIVERS);

        Set driverClasses = new HashSet();

        for (Iterator<String> i = classNames.iterator(); i.hasNext(); ) {
            String className = i.next().toString();

            if (className.toLowerCase().indexOf("driver") == -1) {
                continue;
            }

            try {
                Class c = Class.forName(className, false,
                        Thread.currentThread().getContextClassLoader());
                if (!Driver.class.isAssignableFrom(c)) {
                    continue;
                }

                if (Modifier.isAbstract(c.getModifiers())) {
                    continue;
                }

                // now instantiate and initialize it
                driverClasses.add(c.newInstance());
            } catch (Throwable t) {
            }
        }
        System.out.println("scan complete in "
                + (System.currentTimeMillis() - start) + "ms");
        return (Driver[]) driverClasses.toArray(new Driver[0]);
    }

    private Driver findRegisteredDriver(String url) {
        for (Enumeration drivers = DriverManager.getDrivers(); drivers != null
                && drivers.hasMoreElements(); ) {
            Driver driver = (Driver) drivers.nextElement();
            try {
                if (driver.acceptsURL(url)) {
                    return driver;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    private String constructCmd(String url, String user, String pass, String driver, boolean stripPasswd) {
        String com = "!connect "
                + url + " "
                + (user == null || user.length() == 0 ? "''" : user) + " ";
        if (stripPasswd) {
            com += PASSWD_MASK + " ";
        } else {
            com += (pass == null || pass.length() == 0 ? "''" : pass) + " ";
        }
        com += (driver == null ? "" : driver);
        return com;
    }

    public static void main(String[] args)
            throws IOException, SQLException
    {
        TestDriver testDriver = new TestDriver();
        Driver[] drivers = testDriver.scanDrivers(true);
        System.out.println(drivers.length);
        // org.apache.hive.jdbc.HiveDriver
        System.out.println(drivers[0].getClass().getName());
        String url = "jdbc:hive2://bds-test-001:10000/default";
        if (testDriver.findRegisteredDriver(url) != null) {
            System.out.println(true);
        } else {
            System.out.println(false);
        }

        System.out.println(DriverManager.getDriver(url) != null);
        System.out.println(testDriver.constructCmd(url, "datajingdo_m", "123", "org.apache.hive.jdbc.HiveDriver", false));
    }
}
