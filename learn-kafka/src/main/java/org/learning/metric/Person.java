package org.learning.metric;

/**
 * Created by anderson on 17-11-20.
 */
public class Person implements PersonMBean
{
    private String name;
    private int age;

    public Person(String name, int age)
    {
        this.name = name;
        this.age = age;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getAge()
    {
        return age;
    }

    @Override
    public String sayHello(String hello)
    {
        System.out.println(hello);
        return this.name + ":" + hello;
    }
}
