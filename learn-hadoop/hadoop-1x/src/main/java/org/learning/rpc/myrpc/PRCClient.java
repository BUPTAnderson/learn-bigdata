package org.learning.rpc.myrpc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.ipc.RPC;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by anderson on 16-11-24.
 */
public class PRCClient
{
    private MyRPCProtocol proto;

    public PRCClient()
            throws IOException
    {
        InetSocketAddress addr = new InetSocketAddress("localhost", 8888);
        proto = (MyRPCProtocol) RPC.waitForProxy(MyRPCProtocol.class, 1, addr, new Configuration());
    }
    public void call(String s) {
        System.out.println(proto.test(new Text(s)));
    }

    public static void main(String[] args)
            throws IOException
    {
        PRCClient client = new PRCClient();
        client.call("RPC");
    }
}
