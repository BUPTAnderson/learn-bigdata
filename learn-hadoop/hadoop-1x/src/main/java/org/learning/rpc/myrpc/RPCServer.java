package org.learning.rpc.myrpc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.ipc.RPC;

import java.io.IOException;

/**
 * Created by anderson on 16-11-24.
 */
public class RPCServer implements MyRPCProtocol
{
    RPC.Server server = null;
    public RPCServer()
            throws IOException, InterruptedException
    {
        server = RPC.getServer(this, "localhost", 8888, new Configuration());
        System.out.println("-----调用开始-----");
        server.start();
        server.join();
        System.out.println("-----调用结束-----");
    }
    public Text test(Text t)
    {
        if (t.toString().equals("RPC")) {
            return new Text("1");
        } else {
            return new Text("0");
        }
    }

    public long getProtocolVersion(String s, long l)
            throws IOException
    {
        return 1;
    }

    public static void main(String[] args)
            throws IOException, InterruptedException
    {
        new RPCServer();
    }
}
