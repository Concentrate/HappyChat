package com.nettysocket.pratise.handler;

import com.nettysocket.pratise.manager.NUserManager;
import com.nettysocket.pratise.pojo.NUserInfo;
import com.nettysocket.pratise.protocal.CommonMessage;
import com.nettysocket.pratise.protocal.Extra;
import com.nettysocket.pratise.util.NUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class NMessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        CommonMessage<String> message = CommonMessage.parseResultV2(textWebSocketFrame.text(), String.class);
        NUserInfo info = NUserManager.instance().getUserInfoFromChannel(channelHandlerContext.channel());
        if (info != null) {
            message.setExtra(new Extra().buildFromUserInfo(info));
        }
        NUserManager.instance().brocastChannleMessage(message);
        NUtil.logger.info("server receive a message from {}", channelHandlerContext.channel().remoteAddress());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        NUserManager.instance().removeChannle(ctx.channel());
        NUserManager.instance().brocastUserActiveNumber();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        NUtil.logger.debug("the exception is {}", cause.fillInStackTrace().toString());
    }
}
