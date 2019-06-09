package com.nettysocket.pratise.manager;

import com.nettysocket.pratise.pojo.NUserInfo;
import com.nettysocket.pratise.protocal.CommonMessage;
import com.nettysocket.pratise.protocal.Extra;
import com.nettysocket.pratise.protocal.NMessageProto;
import com.nettysocket.pratise.util.NConstants;
import com.nettysocket.pratise.util.NUtil;
import com.wolfbe.chat.entity.UserInfo;
import com.wolfbe.chat.util.NettyUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * Created by liudeyu on 2019/6/5.
 */
public class NUserManager {
    private static long NOT_ACTIVITE_INTERGAP = 60 * 1000;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock();
    private Map<io.netty.channel.Channel, NUserInfo> useInfoMap = new ConcurrentHashMap<>();
    private AtomicInteger userActivNum = new AtomicInteger();
    private static NUserManager instance;


    private NUserManager() {
    }


    public static synchronized NUserManager instance() {
        if (instance == null) {
            instance = new NUserManager();
        }
        return instance;
    }


    private interface ConcurrentOperation {
        boolean action();
    }

    private interface ChannelAction {
        void action(Channel chan);
    }

    private boolean doConcurrentOperation(ConcurrentOperation operation, boolean writeLock) {
        try {
            if (!writeLock) {
                reentrantLock.readLock().lock();
            } else {
                reentrantLock.writeLock().lock();
            }
            return operation.action();
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return false;
        } finally {
            if (!writeLock) {
                reentrantLock.readLock().unlock();
            } else {
                reentrantLock.writeLock().unlock();
            }
        }
    }

    public void brocastUserActiveNumber(){
        traveUserInfoDoOperation((channel)->{
            doConcurrentOperation(()->{
                channel.writeAndFlush(NMessageProto.buildTextMessage(NMessageProto.SYS,
                        "current member num is {}"+userActivNum.get()).buildJsonMessage());
                return true;
            },true);
        });
    }

    public void brocastChannleMessage(Channel channel, String tmpMess){
            NUserInfo userInfo=useInfoMap.get(channel);
        if(userInfo!=null){
            CommonMessage<String> text=NMessageProto.buildTextMessage(NMessageProto.MESSAGE,tmpMess);
            Extra extra=new Extra();
            extra.setNickeName(userInfo.getNickName());
            extra.setUserId(userInfo.getId());
            extra.setTime(System.currentTimeMillis());
            text.setExtra(extra);
            brocastCommonMessage(text);
        }
    }


    public void brocastCommonMessage(CommonMessage message){
        traveUserInfoDoOperation(chan -> {
            doConcurrentOperation(()->{
                chan.writeAndFlush(new TextWebSocketFrame(message.buildJsonMessage()));
                return true;
            },true);
        });

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
                useInfoMap.put(channel, userInfo);
                return true;
            }
        }, true);
        return this;
    }


    public boolean activiChannel(Channel channel, String nickName) {
        return doConcurrentOperation(new ConcurrentOperation() {
            @Override
            public boolean action() {
                if (!channel.isActive() || !channel.isOpen()) {
                    logger.debug("channel {},is close", NettyUtil.parseChannelRemoteAddr(channel));
                    useInfoMap.remove(channel);
                    return false;
                }
                if(StringUtil.isNullOrEmpty(nickName)){
                    logger.debug("nick name cannot be empty");
                    return false;
                }
                NUserInfo info = useInfoMap.get(channel);
                if (info == null) {
                    info=new NUserInfo();
                    useInfoMap.put(channel,info);
                }

                info.setNickName(nickName);
                info.setAuthen(true);
                info.setTime(System.currentTimeMillis());
                userActivNum.getAndDecrement();
                return true;
            }
        }, true);
    }

    public void removeChannle(Channel channel) {
        doConcurrentOperation(() -> {
            NUserInfo userInfo = useInfoMap.get(channel);
            if (userInfo != null && userInfo.isAuthen()) {
                userActivNum.getAndDecrement();
            }
            useInfoMap.remove(channel);
            if (channel != null) {
                channel.close();
            }
            return true;
        }, true);
    }


    public void cleanNotActivityChannle() {
        doConcurrentOperation(() -> {
            Set<Channel> key = useInfoMap.keySet();
            Set<Channel> needToRemove = new HashSet<>();
            key.forEach(new Consumer<Channel>() {
                @Override
                public void accept(Channel channel) {
                    if (!channel.isOpen() || !channel.isActive()) {
                        needToRemove.add(channel);
                    }
                    NUserInfo userInfo = useInfoMap.get(channel);
                    if (userInfo == null || !userInfo.isAuthen() || System.currentTimeMillis() - userInfo.getTime() > NOT_ACTIVITE_INTERGAP) {
                        needToRemove.add(channel);
                    }
                }
            });

            needToRemove.forEach(new Consumer<Channel>() {
                @Override
                public void accept(Channel channel) {
                    NUtil.logger.info("channel address is being clean {}",channel.remoteAddress());
                    useInfoMap.remove(channel);
                    channel.close();
                }
            });
            return true;
        }, true);
    }

    private void traveUserInfoDoOperation(ChannelAction action) {
        Set<Channel> key = useInfoMap.keySet();
        key.forEach(new Consumer<Channel>() {
            @Override
            public void accept(Channel channel) {
                if (!channel.isOpen() || !channel.isActive()) {
                    return;
                }
                NUserInfo userInfo = useInfoMap.get(channel);
                if (userInfo == null || !userInfo.isAuthen()) {
                    return;
                }
                action.action(channel);
            }
        });
    }

    public void brocastPingOrPongMessage(int pingPongCode) {
        if (pingPongCode != NMessageProto.PING || pingPongCode != NMessageProto.PONG) {
            return;
        }
        doConcurrentOperation(() -> {
            CommonMessage<String> message = (pingPongCode == NMessageProto.PING) ? NMessageProto.buildPingMessage() : NMessageProto.buildPongMessage();
            traveUserInfoDoOperation((action) -> {
                NUtil.logger.info("channel address {} is been notify ping",action.remoteAddress());
                action.writeAndFlush(new TextWebSocketFrame(message.buildJsonMessage()));
            });
            return true;
        }, false);
    }

    public void sendChannelMessage(Channel channel, String message) {
        channel.writeAndFlush(new TextWebSocketFrame(NMessageProto.buildTextMessage(NMessageProto.MESSAGE, message).buildJsonMessage()));
    }


    public void brocastChannleMessage(String message){
        doConcurrentOperation(()->{
            traveUserInfoDoOperation(chan -> {
                sendChannelMessage(chan,message);
            });
            return true;
        },false);
    }

    public void updateChannelInfo(Channel channel){
        NUserInfo userInfo=useInfoMap.get(channel);
        if(userInfo!=null){
            userInfo.setTime(System.currentTimeMillis());
        }
    }


}
