package org.learning;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by anderson on 17-10-9.
 */
public class YarnClientTest
{
//    YarnClient yarnClient;
//    String appName;
    public static void main(String[] args)
            throws IOException, YarnException
    {
        System.out.println(System.getenv("HADOOP_HOME"));
//        System.out.println(System.getProperty("HADOOP_HOME"));
//        System.setProperty("HADOOP_HOME", "/home/anderson/gitJD/hadoop-2.6.1");

        Configuration conf = new Configuration();

        // 如果classpath, resources下有yar-site.xml, 会自动加载进来, 如果名字不是yarn-site.xml, 需要手动加载:
        conf.addResource("dev-yarn-site.xml");
        // 如果文件路径不是在resources下面的话, 需要通过下面的方式加载
        conf.addResource(new FileInputStream("/home/anderson/gitJD/hadoop-2.6.1/etc/hadoop/yarn-site.xml"));

        // addResource(Path file), 加载的是hdfs上路径中的配置文件
        // addResource(URL url), 是通过统一资源定位符来加载配置文件

        conf.set(YarnConfiguration.RM_WEBAPP_DELEGATION_TOKEN_AUTH_FILTER, "false");

        YarnClient yarnClient = YarnClient.createYarnClient();
        yarnClient.init(conf);

        yarnClient.start();

        EnumSet<YarnApplicationState> appStates = EnumSet.noneOf(YarnApplicationState.class);
        if (appStates.isEmpty()) {
            appStates.add(YarnApplicationState.RUNNING);
            appStates.add(YarnApplicationState.ACCEPTED);
            appStates.add(YarnApplicationState.SUBMITTED);
        }

        List<ApplicationReport> applications = yarnClient.getApplications(appStates);
        if (applications.isEmpty()) {
            System.out.println("applications is empty");
            return;
        }

        String appName = "application_1496195987638_0683";
        for (ApplicationReport applicationReport : applications) {
            if (StringUtils.equals(appName, applicationReport.getName())) {
                System.out.println("appReport " + applicationReport);
                System.out.println(applicationReport.getYarnApplicationState().toString());
            }
        }

        yarnClient.stop();
    }
}
