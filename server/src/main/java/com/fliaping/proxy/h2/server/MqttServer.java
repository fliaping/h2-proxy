package com.fliaping.proxy.h2.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttServer {
static Logger logger = LoggerFactory.getLogger(MqttServer.class);
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup boosGroup = new EpollEventLoopGroup(1);
        EventLoopGroup workGroup = new EpollEventLoopGroup(4);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boosGroup,workGroup)
                .channel(EpollServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addFirst("idleStateHandler", new IdleStateHandler(10,10,0))
                        .addLast("decoder", new MqttDecoder())
                .addLast("encoder", MqttEncoder.INSTANCE)
                .addLast("messageLogger", new MQTTMessageLogger())
                .addLast("mqttHandler", new NettyMQTTHandler());
            }
        });

        ChannelFuture future = bootstrap.bind(1234).sync();
        future.channel().closeFuture().sync();
    }
}
