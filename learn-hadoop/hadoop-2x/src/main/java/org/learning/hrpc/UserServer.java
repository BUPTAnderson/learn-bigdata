package org.learning.hrpc;

import com.google.protobuf.BlockingService;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.ProtobufRpcEngine;
import org.apache.hadoop.ipc.RPC;
import org.learning.protobufrpc.proto.UserProto;

import java.io.IOException;

public class UserServer {
    public static String host = "localhost";
    public static int port = 8083;

    public static void main(String[] args) throws IOException {
        Configuration conf = new Configuration();
        RPC.setProtocolEngine(conf, UserServiceProtocol.class, ProtobufRpcEngine.class);
        RPC.Server server = new RPC.Builder(conf)
                .setProtocol(UserServiceProtocol.class)
                .setInstance((BlockingService) UserProto.UserService.newReflectiveBlockingService(new UserServiceProtocolImpl())) //这是最重要的地方， 要强转为BlockingService
                .setBindAddress(host)
                .setPort(port).setNumHandlers(2)
                .setVerbose(false)
                .build();

        server.start();
    }
}
