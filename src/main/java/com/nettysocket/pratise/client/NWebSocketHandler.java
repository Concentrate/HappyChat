package com.nettysocket.pratise.client;

import com.nettysocket.pratise.protocal.CommonMessage;
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
        if(socketClientHandshaker!=null){
            try {
                if (!socketClientHandshaker.isHandshakeComplete()) {
                    FullHttpResponse response = (FullHttpResponse) o;
                    socketClientHandshaker.finishHandshake(channelHandlerContext.channel(),
                            response);
                    shakeResult.setSuccess();
                    return;
                }
            }catch (Exception ex){
                shakeResult.setFailure(ex);
            }
        }
        if (o instanceof WebSocketFrame){
            WebSocketFrame frame=(WebSocketFrame)o;
            if(frame instanceof PingWebSocketFrame||frame instanceof PongWebSocketFrame){
                NUtil.logger.debug("recevie ping or pong frame text");
            }else if(frame instanceof TextWebSocketFrame){
                TextWebSocketFrame textWebSocketFrame=(TextWebSocketFrame)frame;
                CommonMessage<String> message=CommonMessage.parseResultV2(textWebSocketFrame.text(),String.class);
                NUtil.logger.debug("server message is {}",message);

            }else if(frame instanceof CloseWebSocketFrame){
                NUtil.logger.debug("websocket is close");
                channelHandlerContext.channel().close();
            }
        }

    }
}
