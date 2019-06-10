package com.nettysocket.pratise.protocal;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by liudeyu on 2019/6/6.
 */
public class AuthenUser {
    @JSONField(name = "nick")
    private String nickName;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public String toString() {
        return "AuthenUser{" +
                "nickName='" + nickName + '\'' +
                '}';
    }
}
