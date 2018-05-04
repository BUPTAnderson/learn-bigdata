package org.learning.hrpc;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import org.learning.protobufrpc.proto.UserProtos;

public class UserServiceProtocolImpl implements UserServiceProtocol {
    @Override
    public UserProtos.User getUser(RpcController controller, UserProtos.User request) throws ServiceException {
        System.out.println(request.getName());
        UserProtos.User response = UserProtos.User.newBuilder().setId(2).setName("Hello " + request.getName()).setEmail(" your email address: " + request.getEmail()).build();
        return response;
    }
}
