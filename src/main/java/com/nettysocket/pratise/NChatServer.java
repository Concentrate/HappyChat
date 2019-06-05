package com.nettysocket.pratise;

import com.wolfbe.chat.core.BaseServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ServerChannel;
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

    private int port;
    private ScheduledExecutorService scheduledExecutorService;

    public NChatServer(int port) {
        this.port = port;
        scheduledExecutorService= Executors.newScheduledThreadPool(2);
    }



    @Override
    public void start() {
        b.group(bossGroup,workGroup);
        b.option(ChannelOption.SO_KEEPALIVE,true)
                .channel(ServerChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .option(ChannelOption.SO_BACKLOG,1024)
                .childHandler(new ChannelInitializer<io.netty.channel.socket.SocketChannel>() {

                    @Override
                    protected void initChannel(io.netty.channel.socket.SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(1024*1024))
                                .addLast(new ChunkedWriteHandler())
                                .addLast(new IdleStateHandler(60,0,0));
                        // TODO: 2019/6/4 add authen and message
                    }
                });
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {

            }
        },50, TimeUnit.SECONDS);




    }
}
