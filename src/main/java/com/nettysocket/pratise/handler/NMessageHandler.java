package com.nettysocket.pratise.handler;

import com.alibaba.fastjson.JSONObject;
import com.nettysocket.pratise.manager.NUserManager;
import com.nettysocket.pratise.protocal.CommonMessage;
import com.nettysocket.pratise.protocal.NMessageProto;
import com.nettysocket.pratise.util.NUtil;
import com.wolfbe.chat.entity.UserInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class NMessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        CommonMessage<String>message= CommonMessage.parseResultV2(textWebSocketFrame.text(),String.class);
        NUserManager.instance().brocastChannleMessage(message.getData());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        NUserManager.instance().removeChannle(ctx.channel());
        NUserManager.instance().brocastNumber();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        NUtil.logger.debug("the exception is {}",cause.fillInStackTrace().toString());
    }
}