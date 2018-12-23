package org.learning.mapreduce.case9;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class User implements WritableComparable<User> {
    private String userNo = "";
    private String userName = "";
    private String cityNo = "";
    private String cityName = "";
    private int flag = 0;

    public User() {
    }

    public User(String userNo, String userName, String cityNo, String cityName, int flag) {
        this.userNo = userNo;
        this.userName = userName;
        this.cityNo = cityNo;
        this.cityName = cityName;
        this.flag = flag;
    }

    public User(User user) {
        this.userNo = user.getUserNo();
        this.userName = user.getUserName();
        this.cityNo = user.getCityNo();
        this.cityName = user.getCityName();
        this.flag = user.getFlag();
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCityNo() {
        return cityNo;
    }

    public void setCityNo(String cityNo) {
        this.cityNo = cityNo;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    @Override
    public int compareTo(User o) {
        return 0;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(userNo);
        out.writeUTF(userName);
        out.writeUTF(cityNo);
        out.writeUTF(cityName);
        out.writeInt(flag);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.userNo = in.readUTF();
        this.userName = in.readUTF();
        this.cityNo = in.readUTF();
        this.cityName = in.readUTF();
        this.flag = in.readInt();
    }

    @Override
    public String toString() {
        return userNo + "\t" + userName + "\t" + cityName;
    }
}
