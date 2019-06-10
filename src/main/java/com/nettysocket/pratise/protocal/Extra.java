package com.nettysocket.pratise.protocal;

import com.alibaba.fastjson.annotation.JSONField;
import com.nettysocket.pratise.pojo.NUserInfo;
import com.wolfbe.chat.util.DateTimeUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by liudeyu on 2019/6/6.
 */
public class Extra {

    @JSONField(name = "nick")
    private String nickeName;
    @JSONField(name = "time")
    private long time;

    @JSONField(name = "user_id")
    private long userId;


    public Extra() {
    }

    public Extra buildFromUserInfo(NUserInfo info) {
        if (info != null) {
            nickeName = info.getNickName();
            time = info.getTime();
            userId = info.getId();
        }
        return this;
    }

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
        String dateFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        return "nickname:'" + nickeName + '\'' +
                ", time:" + format.format(new Date(time)) +
                ", current user id:" + userId ;
    }
}
