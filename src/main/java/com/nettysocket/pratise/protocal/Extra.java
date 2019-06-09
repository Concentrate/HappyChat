package com.nettysocket.pratise.protocal;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by liudeyu on 2019/6/6.
 */
public class Extra {

    @JSONField(name = "nick")
    private String nickeName;
    @JSONField(name="time")
    private long time;

    @JSONField(name = "user_id")
    private long userId;

    public String getNickeName() {
        return nickeName;
    }

    public void setNickeName(String nickeName) {
        this.nickeName = nickeName;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Extra{" +
                "nickeName='" + nickeName + '\'' +
                ", time=" + time +
                ", userId=" + userId +
                '}';
    }
}
