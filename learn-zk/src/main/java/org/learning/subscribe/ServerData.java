package org.learning.subscribe;

/**
 * 服务器基本信息
 * Created by Anderson on 2018/9/18
 */
public class ServerData {
    private String address;
    private Integer id;
    private String name;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ServerData [address=" + address + ", id=" + id + ", name="
                + name + "]";
    }
}
