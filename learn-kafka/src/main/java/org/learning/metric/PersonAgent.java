package org.learning.metric;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import java.lang.management.ManagementFactory;

/**
 * Created by anderson on 17-11-20.
 */
public class PersonAgent
{
    public static void main(String[] args)
            throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException
    {
        // 获取MBeanServer对象
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName personName = new ObjectName("jmxBean:name=xiaoming");
//        server.registerMBean(new Person("xiaoming", 27), personName);
        server.registerMBean(new PersonDynamic(new Person("xiaoming", 27)), personName);
        Thread.sleep(60 * 60 * 1000);
    }
}
