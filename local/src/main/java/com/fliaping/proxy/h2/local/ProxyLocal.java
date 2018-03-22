package com.fliaping.proxy.h2.local;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyLocal {

    private static Logger logger = LoggerFactory.getLogger(ProxyLocal.class);

    private static final int LOCAL_PORT = 9999;
    private static String REMOTE_HOST = "www.google.com";
    private static final int REMOTE_PORT = 443;

    public static void main(String[] args) {
        logger.info("Proxying *:" + LOCAL_PORT + " to " + REMOTE_HOST + ':' + REMOTE_PORT + " ...");
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ProxyInitializer(REMOTE_HOST, REMOTE_PORT))
                    .bind(LOCAL_PORT).sync().channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            logger.info("Stop Proxy");

        }
    }
}
