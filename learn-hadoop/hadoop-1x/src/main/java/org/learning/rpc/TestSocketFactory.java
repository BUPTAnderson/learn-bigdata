package org.learning.rpc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ReflectionUtils;
import org.learning.rpc.myrpc.MyRPCProtocol;

import javax.net.SocketFactory;

/**
 * Created by anderson on 17-7-9.
 */
public class TestSocketFactory
{
    public static javax.net.SocketFactory getSocketFactory(Configuration conf,
            Class<?> clazz) {
        javax.net.SocketFactory factory = null;

        String propValue =
                conf.get("hadoop.rpc.socket.factory.class." + clazz.getSimpleName());
        if ((propValue != null) && (propValue.length() > 0))
            factory = getSocketFactoryFromProperty(conf, propValue);

        if (factory == null)
            factory = getDefaultSocketFactory(conf);

        return factory;
    }

    public static javax.net.SocketFactory getSocketFactoryFromProperty(
            Configuration conf, String propValue) {
        try {
            Class<?> theClass = conf.getClassByName(propValue);
            return (javax.net.SocketFactory) ReflectionUtils.newInstance(theClass, conf);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Socket Factory class not found: " + cnfe);
        }
    }

    public static javax.net.SocketFactory getDefaultSocketFactory(Configuration conf) {
        String propValue = conf.get("hadoop.rpc.socket.factory.class.default");
        if ((propValue == null) || (propValue.length() == 0))
            return javax.net.SocketFactory.getDefault();

        return getSocketFactoryFromProperty(conf, propValue);
    }

    public static void main(String[] args)
    {
        Configuration conf = new Configuration();
        SocketFactory socketFactory = getSocketFactory(conf, MyRPCProtocol.class);
        System.out.println(socketFactory.getClass().getName());
    }
}
