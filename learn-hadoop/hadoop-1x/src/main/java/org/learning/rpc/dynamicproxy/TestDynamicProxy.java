package org.learning.rpc.dynamicproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by anderson on 17-2-24.
 */

//动态代理的使用
interface Subject {
    void action(String name);
}

//被代理类
class RealSubject implements Subject {
    @Override
    public void action(String name)
    {
        System.out.println("RealSubject name=" + name);
        System.out.println("我是被代理类，记得要执行我哦！");
    }
}

/*
 * 这是一个动态代理类
 */
class MyInvocationHandler implements InvocationHandler
{
    //实现了接口的被代理类的对象的声明
    Object obj;

    //1.给被代理的对象实例化  2.返回一个代理类的对象
    public Object blind(Object obj) {
        this.obj = obj;
        //newProxyInstance()返回一个指定接口的代理类实例，该接口可以将方法调用指派到指定的调用处理程序。
        Object o = Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(), this);
        return o;
    }

    //（被代理类执行方法之前会执行该方法，是通过上面的newProxyInstance()方法中的this调用下面的方法）
    //当通过代理类的对象发起对被重写的方法的调用时，都会转化为对如下的 invoke()方法的调用；
    //proxy:在其上调用方法的代理实例
    //method:对应于在代理实例上调用的接口方法的Method实例
    //args：参数
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
    {
        System.out.println("Object:" + proxy.getClass().getName());
        System.out.println("Method:" + method.getName());
        if (args != null) {
            for (Object o : args) {
                System.out.println("arg:" + o);
            }
        }
        // method方法的返回值是returnVal
        Object returnVal = method.invoke(obj, args);
        return returnVal;
    }
}

public class TestDynamicProxy
{
    public static void main(String[] args)
    {
        //1.被代理类的对象
        RealSubject realSubject = new RealSubject();
        //2.创建一个实现了InvocationHandler接口的类的对象
        MyInvocationHandler handler = new MyInvocationHandler();
        //3.调用blind()方法，返回一个同样实现了real所在类实现的接口的代理类的对象
        Object object = handler.blind(realSubject);
        System.out.println("-- object class:" + object.getClass().getName());
        //此时的sub就是代理类的对象
        Subject subject = (Subject) object;
        //调用action()方法之前会转到对InvacationHandler接口的实现类的invoke()方法的调用,其中method就是action()方法
        subject.action("zhangsan");
    }
}
