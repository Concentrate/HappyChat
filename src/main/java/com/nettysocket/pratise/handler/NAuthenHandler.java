package com.nettysocket.pratise.handler;

import com.alibaba.fastjson.JSONObject;
import com.nettysocket.pratise.manager.NUserManager;
import com.nettysocket.pratise.protocal.AuthenUser;
import com.nettysocket.pratise.protocal.CommonMessage;
import com.nettysocket.pratise.protocal.NMessageProto;
import com.nettysocket.pratise.util.NConstants;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NAuthenHandler extends SimpleChannelInboundHandler<Object> {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

        if (o instanceof FullHttpRequest) {
            handleHttpRequest(channelHandlerContext, o);
        } else if (o instanceof WebSocketFrame) {
            handleWebSocketFrame(channelHandlerContext, o);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        logger.debug("user event is trigger");
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event=(IdleStateEvent)evt;
            if(event.state()== IdleState.READER_IDLE){
                Channel channel=ctx.channel();
                channel.close();
                NUserManager.instance().removeChannle(channel);
                NUserManager.instance().brocastUserActiveNumber();
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    private void handleWebSocketFrame(ChannelHandlerContext channelHandlerContext, Object o) {
        WebSocketFrame webSocketFrame = (WebSocketFrame) o;
        if (webSocketFrame instanceof PingWebSocketFrame) {
            channelHandlerContext.channel().writeAndFlush(new PingWebSocketFrame());
            return;
        } else if (webSocketFrame instanceof PongWebSocketFrame) {
            channelHandlerContext.channel().writeAndFlush(new PongWebSocketFrame());
            return;
        }else if(webSocketFrame instanceof CloseWebSocketFrame){
            NUserManager.instance().removeChannle(channelHandlerContext.channel());
            NUserManager.instance().brocastUserActiveNumber();
            channelHandlerContext.channel().close();
            return;
        }


        if (!(webSocketFrame instanceof TextWebSocketFrame)) {
            logger.debug("only text support for now");
            channelHandlerContext.channel().close();
            return;
        }
        TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) o;
        JSONObject jsonObject = JSONObject.parseObject(textWebSocketFrame.text());
        int code = jsonObject.getInteger("code");
        switch (code) {
            case NMessageProto.PING:
                NUserManager.instance().updateChannelInfo(channelHandlerContext.channel());
                NUserManager.instance().sendChannelMessage(channelHandlerContext.channel(),
                        NMessageProto.buildPingMessage().buildJsonMessage());
                break;
            case NMessageProto.PONG:
                NUserManager.instance().updateChannelInfo(channelHandlerContext.channel());
                NUserManager.instance().sendChannelMessage(channelHandlerContext.channel(),
                        NMessageProto.buildPongMessage().buildJsonMessage());
                break;
            case NMessageProto.AUTHEN:
                CommonMessage<AuthenUser> commonMessage = CommonMessage.parseResultV2(jsonObject.toJSONString(),
                        AuthenUser.class);
                if (commonMessage.getData() == null) {
                    break;
                }
                logger.debug("log user is {}", commonMessage.getData().getNickName());
                boolean succ = NUserManager.instance().activiChannel(channelHandlerContext.channel(), commonMessage.getData()
                        .getNickName());
                NUserManager.instance().sendChannelMessage(channelHandlerContext.channel(),"is success:"+succ);
                NUserManager.instance().brocastUserActiveNumber();
                break;
            case NMessageProto.MESSAGE:
                // TODO: 2019/6/6 给MessageHandler处理
                channelHandlerContext.fireChannelRead(webSocketFrame.retain());
                break;
        }
    }

    private void handleHttpRequest(ChannelHandlerContext channelHandlerContext, Object o) {
        FullHttpRequest request = (FullHttpRequest) o;
        if (request == null || !request.headers().get(HttpHeaderNames.UPGRADE).contentEquals(HttpHeaderValues.WEBSOCKET)) {
            logger.debug("don't upgrade to websocket");
            channelHandlerContext.channel().close();
            return;
        }

        WebSocketServerHandshakerFactory serverHandshakerFactory = new WebSocketServerHandshakerFactory(NConstants.WEBSOCKET_URL,
                null, true);
        WebSocketServerHandshaker handshaker = serverHandshakerFactory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channelHandlerContext.channel());
        } else {
            handshaker.handshake(channelHandlerContext.channel(), request);
            NUserManager.instance().addChannel(channelHandlerContext.channel());
        }
    }


}
