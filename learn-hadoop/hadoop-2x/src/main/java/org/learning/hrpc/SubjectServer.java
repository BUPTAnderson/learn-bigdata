package org.learning.hrpc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.learning.proxy.Subject;
import org.learning.proxy.SubjectImpl;

import java.io.IOException;

public class SubjectServer {
    public static String host = "localhost";
    public static int port = 8083;
    public static void main(String[] args) throws IOException {
        Configuration conf = new Configuration();
        RPC.Server server = new RPC.Builder(conf)
                .setProtocol(Subject.class)
                .setInstance(new SubjectImpl())
                .setBindAddress(host)
                .setPort(port).setNumHandlers(2)
                .setVerbose(false)
//                .setSecretManager(namesystem.getDelegationTokenSecretManager())
                .build();

        server.start();
    }
}
