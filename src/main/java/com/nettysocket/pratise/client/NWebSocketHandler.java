package com.nettysocket.pratise.client;

import com.alibaba.fastjson.JSONObject;
import com.nettysocket.pratise.manager.NUserManager;
import com.nettysocket.pratise.protocal.AuthenUser;
import com.nettysocket.pratise.protocal.CommonMessage;
import com.nettysocket.pratise.protocal.NMessageProto;
import com.nettysocket.pratise.util.NUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;

public class NWebSocketHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketClientHandshaker socketClientHandshaker;
    private ChannelPromise shakeResult;

    public WebSocketClientHandshaker getSocketClientHandshaker() {
        return socketClientHandshaker;
    }

    public void setSocketClientHandshaker(WebSocketClientHandshaker socketClientHandshaker) {
        this.socketClientHandshaker = socketClientHandshaker;
    }

    public ChannelPromise getShakeResult() {
        return shakeResult;
    }

    public void setShakeResult(ChannelPromise shakeResult) {
        this.shakeResult = shakeResult;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        if (socketClientHandshaker != null) {
            try {
                if (!socketClientHandshaker.isHandshakeComplete()) {
                    FullHttpResponse response = (FullHttpResponse) o;
                    socketClientHandshaker.finishHandshake(channelHandlerContext.channel(),
                            response);
                    shakeResult.setSuccess();
                    return;
                }
            } catch (Exception ex) {
                shakeResult.setFailure(ex);
            }
        }
        if (o instanceof WebSocketFrame) {
            WebSocketFrame frame = (WebSocketFrame) o;
            if (frame instanceof PingWebSocketFrame) {
                NUtil.logger.debug("recevie ping frame text");
                channelHandlerContext.channel().writeAndFlush(new PongWebSocketFrame());
            } else if (frame instanceof PongWebSocketFrame) {
                NUtil.clientLogger.info("receive server pong message,server is alive");
            } else if (frame instanceof TextWebSocketFrame) {
                String text = ((TextWebSocketFrame) frame).text();
                JSONObject jsonObject = JSONObject.parseObject(text);
                int code = jsonObject.getInteger("code");
                switch (code) {
                    case NMessageProto.AUTHEN:
                        CommonMessage<AuthenUser> authenUserCommonMessage = CommonMessage.parseResultV2(text, AuthenUser.class);
                        NUtil.clientLogger.info("server authen info is {}", authenUserCommonMessage);
                        break;
                    default:
                        CommonMessage<String> message = CommonMessage.parseResultV2(text, String.class);
                        NUtil.logger.debug("from server message is {}", message);
                        break;
                }

            } else if (frame instanceof CloseWebSocketFrame) {
                NUtil.logger.debug("websocket is close");
                channelHandlerContext.channel().close();
            }
        }

    }
}
