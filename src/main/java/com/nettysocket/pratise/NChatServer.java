package com.nettysocket.pratise;

import com.nettysocket.pratise.handler.NAuthenHandler;
import com.nettysocket.pratise.handler.NMessageHandler;
import com.nettysocket.pratise.manager.NUserManager;
import com.nettysocket.pratise.protocal.NMessageProto;
import com.nettysocket.pratise.util.NUtil;
import com.wolfbe.chat.core.BaseServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by liudeyu on 2019/6/4.
 */
public class NChatServer extends BaseServer {

    private ScheduledExecutorService scheduledExecutorService;


    public NChatServer(int port) {
        init();
        this.port = port;
        scheduledExecutorService = Executors.newScheduledThreadPool(2);
    }


    @Override
    public void start() {
        b.group(bossGroup, workGroup);
        b.option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .localAddress(port)
                .childHandler(new ChannelInitializer<io.netty.channel.socket.SocketChannel>() {

                    @Override
                    protected void initChannel(io.netty.channel.socket.SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(1024 * 1024))
                                .addLast(new ChunkedWriteHandler())
                                .addLast(new IdleStateHandler(180, 0, 0));
                        socketChannel.pipeline().addLast(new NAuthenHandler())
                                .addLast(new NMessageHandler());

                    }
                });
        try {
            Channel client = b.bind().sync().channel();
            NUtil.logger.debug("current client local ip is {},and the " +
                    "remote address {}", client.localAddress(), client.remoteAddress());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                NUtil.logger.info("start scan not activite channel");
                NUserManager.instance().cleanNotActivityChannle();
            }
        }, 3 * 60, 60, TimeUnit.SECONDS);

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            NUtil.logger.info("start browsecast ping or pong channel");
            NUserManager.instance().brocastPingOrPongMessage(NMessageProto.PING);
        }, 5, 50, TimeUnit.SECONDS);

    }
}
