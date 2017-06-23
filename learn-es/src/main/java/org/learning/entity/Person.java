/**
 * @author Geloin
 */
package org.learning.entity;

/**
 * 测试使用的实体
 *
 * @author Geloin
 */
public class Person
{
    /**
     * 名称
     */
    private String name;

    /**
     * 性别
     */
    private String sex;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 是否学生
     */
    private boolean student;
    /**
     * info
     */
    private String info;
    /**
     * 出生年 yyyy-MM-dd
     */
    private String dateOfBirth;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSex()
    {
        return sex;
    }

    public void setSex(String sex)
    {
        this.sex = sex;
    }

    public Integer getAge()
    {
        return age;
    }

    public void setAge(Integer age)
    {
        this.age = age;
    }

    public boolean isStudent()
    {
        return student;
    }

    public void setStudent(boolean student)
    {
        this.student = student;
    }

    public String getInfo()
    {
        return info;
    }

    public void setInfo(String info)
    {
        this.info = info;
    }

    public String getDateOfBirth()
    {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth)
    {
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public String toString()
    {
        return "Person{" +
                "name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", age=" + age +
                ", student=" + student +
                ", info='" + info + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                '}';
    }
}
