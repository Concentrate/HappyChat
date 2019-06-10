package com.nettysocket.pratise.pojo;

import io.netty.channel.Channel;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by liudeyu on 2019/6/5.
 */
public class NUserInfo {

    private static AtomicLong idGen = new AtomicLong(1);

    private Channel channel;
    private String nickName = "";
    private String remoteAddress = "";
    private long time;
    private long id;
    private boolean isAuthen;

    public NUserInfo() {

    }

    public static AtomicLong getIdGen() {
        return idGen;
    }

    public static void setIdGen(AtomicLong idGen) {
        NUserInfo.idGen = idGen;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isAuthen() {
        return isAuthen;
    }

    public void setAuthen(boolean authen) {
        isAuthen = authen;
    }

    public long getId() {
        return id;
    }

    public void generateIdAtom() {
        id = idGen.getAndIncrement();
    }

    @Override
    public String toString() {
        return "NUserInfo{" +
                "channel=" + channel +
                ", nickName='" + nickName + '\'' +
                ", remoteAddress='" + remoteAddress + '\'' +
                ", time=" + time +
                ", id=" + id +
                ", isAuthen=" + isAuthen +
                '}';
    }
}
