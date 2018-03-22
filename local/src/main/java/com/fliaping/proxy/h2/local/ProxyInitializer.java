package com.fliaping.proxy.h2.local;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socks.SocksInitRequestDecoder;
import io.netty.handler.codec.socks.SocksMessageEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ProxyInitializer extends ChannelInitializer<SocketChannel> {

    private final String remoteHost;
    private final int remotePort;

    public ProxyInitializer(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO))
                .addLast(new SocksInitRequestDecoder())
                .addLast(new SocksMessageEncoder())
                .addLast(new SocksInitHandler(remoteHost, remotePort));
    }
}
