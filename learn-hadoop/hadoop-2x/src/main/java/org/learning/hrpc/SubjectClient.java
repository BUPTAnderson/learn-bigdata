package org.learning.hrpc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.learning.proxy.Subject;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SubjectClient {
    public static void main(String[] args) throws IOException {
        Configuration conf = new Configuration();
        Subject proxy = RPC.getProxy(Subject.class, 1L, new InetSocketAddress(SubjectServer.host, SubjectServer.port), conf);
        System.out.println(proxy.sayHello());
        proxy.helloWorld();
    }
}
