package org.learning.hrpc;

import org.apache.hadoop.ipc.ProtocolInfo;
import org.learning.protobufrpc.proto.UserProto;

@ProtocolInfo(protocolName =
        "org.learning.hrpc.UserServiceProtocol",
        protocolVersion = 1)
public interface UserServiceProtocol extends UserProto.UserService.BlockingInterface {
}
