package org.learning.rpc.myrpc;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.ipc.VersionedProtocol;

/**
 * Created by anderson on 16-11-24.
 */
public interface MyRPCProtocol extends VersionedProtocol
{
    Text test(Text t);
}
