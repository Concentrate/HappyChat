package com.nettysocket.pratise.client;

import com.nettysocket.pratise.protocal.AuthenUser;
import com.nettysocket.pratise.protocal.CommonMessage;
import com.nettysocket.pratise.protocal.NMessageProto;
import com.nettysocket.pratise.util.NConstants;
import com.nettysocket.pratise.util.NUtil;
import com.wolfbe.chat.HappyChatMain;
import com.wolfbe.chat.HappyChatServer;
import com.wolfbe.chat.util.Constants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker13;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.StringUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

public class NClient {

    private static final String WEBSOCKET_CHANNLE_KEY = "web_socketchannle_key";
    private static final long MAX_PAYLOADS = 1024 * 1024;

    public void start(String url) {
        EventLoopGroup e = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(e)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(10 * 1024 * 1024))
                                .addLast(WEBSOCKET_CHANNLE_KEY, new NWebSocketHandler());

                    }
                });
        try {
            URI uri = new URI(url);
            DefaultHttpHeaders headers = new DefaultHttpHeaders();
            WebSocketClientHandshaker13 socketClientHandshaker13 = new WebSocketClientHandshaker13(uri, WebSocketVersion.V13,
                    "", true, headers, Long.valueOf(MAX_PAYLOADS).intValue());
            Channel channel = bootstrap.connect(uri.getHost(), uri.getPort()).sync().channel();
            NWebSocketHandler myClientHanlder = (NWebSocketHandler) channel.pipeline().get(WEBSOCKET_CHANNLE_KEY);
            myClientHanlder.setSocketClientHandshaker(socketClientHandshaker13);
            myClientHanlder.setShakeResult(new DefaultChannelPromise(channel));
            socketClientHandshaker13.handshake(channel);
            myClientHanlder.getShakeResult().sync();
            Scanner scanner = new Scanner(System.in);
            NUtil.logger.info("start to input message info");
            boolean authen = false;
            while (true) {
                if (!authen) {
                    System.out.println("please input your name  ");
                    String text = scanner.nextLine();
                    if (StringUtil.isNullOrEmpty(text)) {
                        System.out.println("name cannot be empty");
                        continue;
                    }
                    AuthenUser user = new AuthenUser();
                    user.setNickName(text);
                    CommonMessage<AuthenUser> authenUserCommonMessage = NMessageProto.buildAuthenMessage(NMessageProto.AUTHEN, user);
                    authen = true;
                    channel.writeAndFlush(new TextWebSocketFrame(authenUserCommonMessage.buildJsonMessage()));
                    System.out.println("authen message send ");
                    continue;
                }
                String text = scanner.nextLine();
                TextWebSocketFrame socketFrame = new TextWebSocketFrame(NMessageProto.buildTextMessage(NMessageProto.MESSAGE,
                        text).buildJsonMessage());
                channel.writeAndFlush(socketFrame).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        NUtil.logger.debug("message write send completely");
                    }
                });
            }

        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }
}
