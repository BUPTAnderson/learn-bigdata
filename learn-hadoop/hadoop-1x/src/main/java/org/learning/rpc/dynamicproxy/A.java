package org.learning.rpc.dynamicproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by anderson on 16-11-23.
 */
public class A implements CalculationProtocol
{
    public int add(int a, int b)
    {
        return a + b;
    }

    public int sub(int a, int b)
    {
        return a - b;
    }

    public static void main(String[] args)
    {
        A a = new A();
        InvocationHandler handler = new CalculationHandler(a);
        CalculationProtocol aProxy = (CalculationProtocol) Proxy.newProxyInstance(a.getClass().getClassLoader(), a.getClass().getInterfaces(), handler);
        System.out.println(aProxy.add(3, 4));
    }
}

class CalculationHandler implements InvocationHandler {
    private Object originalObj;

    public CalculationHandler(Object obj)
    {
        this.originalObj = obj;
    }

    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
    {
        System.out.println("before call invoke:");
        Object res = method.invoke(originalObj, args);
        System.out.println("after call invoke");
        return res;
    }
}