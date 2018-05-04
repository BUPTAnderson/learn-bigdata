package org.learning.hrpc;

import com.google.protobuf.ServiceException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.ProtobufRpcEngine;
import org.apache.hadoop.ipc.RPC;
import org.learning.protobufrpc.proto.UserProtos;

import java.io.IOException;
import java.net.InetSocketAddress;

public class UserClient {
    public static void main(String[] args) throws IOException, ServiceException {
        Configuration conf = new Configuration();
        RPC.setProtocolEngine(conf, UserServiceProtocol.class, ProtobufRpcEngine.class);
        UserServiceProtocol userService = (UserServiceProtocol) RPC.getProxy(UserServiceProtocol.class, 1L, new InetSocketAddress(SubjectServer.host, SubjectServer.port), conf);
        UserProtos.User request = UserProtos.User.newBuilder().setEmail("kyl@126.com").setName("anderson").setId(1).build();
        UserProtos.User user = userService.getUser(null, request);
        System.out.println("name:" + user.getName() + ", email:" + user.getEmail());
    }
}
