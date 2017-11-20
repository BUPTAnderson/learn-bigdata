package org.learning.metric;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anderson on 17-11-20.
 */
public class PersonDynamic implements DynamicMBean
{
    // Person对象
    private Person person;
    // 描述属性信息
    private List<MBeanAttributeInfo> attributes = new ArrayList<>();
    // 描述构造器信息
    private List<MBeanConstructorInfo> constructors = new ArrayList<>();
    // 描述方法信息
    private List<MBeanOperationInfo> operations = new ArrayList<>();
    // 描述通知信息
    private List<MBeanNotificationInfo> notifications = new ArrayList<>();
    // MBeanInfo用与管理以上描述信息
    private MBeanInfo mBeanInfo;

    public PersonDynamic(Person person)
    {
        this.person = person;
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws Exception {
        // 构建Person的属性,方法,构造器等信息
        constructors.add(new MBeanConstructorInfo("PersonDynamic(String,Integer)构造器", this.person.getClass().getConstructors()[0]));
        attributes.add(new MBeanAttributeInfo("name", "java.lang.String", "姓名", true, false, false));
        attributes.add(new MBeanAttributeInfo("age", "int", "年龄", true, false, false));
        operations.add(new MBeanOperationInfo("sayHello()方法", this.person.getClass().getMethod("sayHello", new Class[]{String.class})));
        // 创建一个MBeanInfo对象
        this.mBeanInfo = new MBeanInfo(this.getClass().getName(), "PersonDynamic", attributes.toArray(new MBeanAttributeInfo[attributes.size()]), constructors.toArray(new MBeanConstructorInfo[constructors.size()]), operations.toArray(new MBeanOperationInfo[operations.size()]), notifications.toArray(new MBeanNotificationInfo[notifications.size()]));
    }

    @Override
    public Object getAttribute(String attribute)
            throws AttributeNotFoundException, MBeanException, ReflectionException
    {
        // 获取person对象属性值
        if (attribute.equals("name")) {
            return this.person.getName();
        } else if (attribute.equals("age")) {
            return this.person.getAge();
        }
        return null;
    }

    @Override
    public void setAttribute(Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        //
    }

    @Override
    public AttributeList getAttributes(String[] attributes)
    {
        // 通过属性名获取一个属性对象列表
        if (attributes == null || attributes.length == 0) {
            return null;
        }
        try {
            AttributeList attrList = new AttributeList();
            for (String attrName : attributes) {
                Object obj = this.getAttribute(attrName);
                Attribute attribute = new Attribute(attrName, obj);
                attrList.add(attribute);
            }
            return attrList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes)
    {
        return null;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature)
            throws MBeanException, ReflectionException
    {
        // 调用Person里面指定的方法
        if (actionName.equals("sayHello")) {
            return this.person.sayHello(params[0].toString());
        }
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo()
    {
        // 获取MBeanInfo
        return mBeanInfo;
    }
}
