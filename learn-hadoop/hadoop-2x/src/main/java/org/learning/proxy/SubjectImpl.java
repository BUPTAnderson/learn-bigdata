package org.learning.proxy;

public class SubjectImpl implements Subject {
    @Override
    public int sayHello() {
        return 10;
    }

    @Override
    public void helloWorld() {
        System.out.println("Hello world");
    }
}
