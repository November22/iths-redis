package com.iths.redis.pubsub;

import java.io.Serializable;

/**
 * 被redis pub 的对象必须序列化
 * @author sen.huang
 * @date 2019/1/26.
 */
public class UserInfo implements Serializable{

    private static final long serialVersionUID = 2089827652784633872L;

    public UserInfo(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String name;

    public int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
