package org.learning.rpc.myrpc;

import org.apache.hadoop.security.SecurityUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by anderson on 17-7-9.
 */
public class TestInetSocketAddress
{
    private static Map<String, String> hostToResolved =
            new HashMap<String, String>();

    public static String getStaticResolution(String host) {
        synchronized (hostToResolved) {
            return hostToResolved.get(host);
        }
    }

    public static InetSocketAddress makeSocketAddr(String host, int port) {
        String staticHost = getStaticResolution(host);
        String resolveHost = (staticHost != null) ? staticHost : host;

        InetSocketAddress addr;
        try {
            InetAddress iaddr = SecurityUtil.getByName(resolveHost);
            // if there is a static entry for the host, make the returned
            // address look like the original given host
            if (staticHost != null) {
                iaddr = InetAddress.getByAddress(host, iaddr.getAddress());
            }
            addr = new InetSocketAddress(iaddr, port);
        } catch (UnknownHostException e) {
            addr = InetSocketAddress.createUnresolved(host, port);
        }
        return addr;
    }

    public static void main(String[] args)
    {
        String target = "anderson-JD:9001";
        int defaultPort = -1;
        if (target == null) {
            throw new IllegalArgumentException("Socket address is null");
        }
        boolean hasScheme = target.contains("://");
        URI uri = null;
        try {
            uri = hasScheme ? URI.create(target) : URI.create("dummyscheme://" + target);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Does not contain a valid host:port authority: " + target
            );
        }

        String host = uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            port = defaultPort;
        }
        String path = uri.getPath();

        if ((host == null) || (port < 0) ||
                (!hasScheme && path != null && !path.isEmpty())) {
            throw new IllegalArgumentException(
                    "Does not contain a valid host:port authority: " + target
            );
        }
        InetSocketAddress socketAddress = makeSocketAddr(host, port);
    }
}
