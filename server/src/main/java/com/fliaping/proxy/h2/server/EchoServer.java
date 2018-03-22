package com.fliaping.proxy.h2.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Locale;

public class EchoServer {

    private static final String WEBSOCKET_PATH = "/websocket";
    private static final System.Logger logger = System.getLogger(EchoServer.class.getName());

    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            pipeline.addLast(new WebSocketServerCompressionHandler());
                            pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
                            pipeline.addLast(new WebSocketIndexPageHandler(WEBSOCKET_PATH));
                            pipeline.addLast(new SimpleChannelInboundHandler<WebSocketFrame>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
                                    if (msg instanceof TextWebSocketFrame) {
                                        String request = ((TextWebSocketFrame) msg).text();
                                        logger.log(System.Logger.Level.INFO,ctx.channel() + " received " + request);
                                        ctx.channel().writeAndFlush(new TextWebSocketFrame(request.toUpperCase(Locale.CHINESE)));
                                    } else {
                                        String message = "unsupported frame type: "+ msg.getClass().getName();
                                        throw new UnsupportedOperationException(message);
                                    }
                                }
                            });
                        }
                    });

            ChannelFuture f = bootstrap.bind(12345).sync();

            f.channel().closeFuture().sync();
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
