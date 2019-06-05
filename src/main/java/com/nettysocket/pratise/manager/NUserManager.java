package com.nettysocket.pratise.manager;

import com.nettysocket.pratise.pojo.NUserInfo;
import com.nettysocket.pratise.util.NConstants;
import com.wolfbe.chat.util.NettyUtil;
import io.netty.channel.Channel;
import io.netty.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by liudeyu on 2019/6/5.
 */
public class NUserManager {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ReentrantLock reentrantLock = new ReentrantLock();
    private Map<io.netty.channel.Channel, NUserInfo> useInfos = new ConcurrentHashMap<>();
    private AtomicInteger userActivNum = new AtomicInteger();
    private NUserManager instance;

    private NUserManager() {
    }


    public synchronized NUserManager instance() {
        if (instance == null) {
            instance = new NUserManager();
        }
        return instance;
    }


    private interface ConcurrentOperation {
        boolean action();
    }

    private boolean doConcurrentOperation(ConcurrentOperation operation) {
        try {
            reentrantLock.lock();
            return operation.action();
        } catch (Exception e) {
            logger.debug(NConstants.ERROR_LOG, e.getMessage());
            return false;
        } finally {
            reentrantLock.unlock();
        }
    }


    public NUserManager addChannel(Channel channel) {
        doConcurrentOperation(new ConcurrentOperation() {
            @Override
            public boolean action() {
                String remoteAddress = NettyUtil.parseChannelRemoteAddr(channel);
                logger.debug("add remote address is {}", remoteAddress);
                NUserInfo userInfo = new NUserInfo();
                userInfo.setChannel(channel);
                userInfo.setRemoteAddress(remoteAddress);
                userInfo.setTime(System.currentTimeMillis());
                useInfos.put(channel, userInfo);
                return true;
            }
        });
        return this;
    }


    public boolean activiChannel(Channel channel, String nickName) {
        return doConcurrentOperation(new ConcurrentOperation() {
            @Override
            public boolean action() {
                if (!channel.isActive() || !channel.isOpen()) {
                    logger.debug("channel {},is close", NettyUtil.parseChannelRemoteAddr(channel));
                    return false;
                }

                NUserInfo info = useInfos.get(channel);
                if (info == null) {
                    return false;
                }

                info.setNickName(nickName);
                info.setAuthen(true);
                info.setTime(System.currentTimeMillis());
                userActivNum.getAndDecrement();
                return true;
            }
        });
    }


    public void cleanNotActivityChannle(){

    }








}
