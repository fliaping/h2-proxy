package com.fliaping.proxy.h2.local;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.socks.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.fliaping.proxy.h2.local.ChannelUtils.closeOnFlush;

public class SocksInitHandler extends SimpleChannelInboundHandler<SocksRequest> {
    private Logger logger = LoggerFactory.getLogger(SocksInitHandler.class);

    private final String remoteHost;
    private final int remotePort;

    public SocksInitHandler(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocksRequest msg) throws Exception {
        switch (msg.requestType()) {
            case INIT:
                logger.info("socks server init");
                ctx.pipeline().addFirst(new SocksCmdRequestDecoder());
                ctx.writeAndFlush(new SocksInitResponse(SocksAuthScheme.NO_AUTH));
                break;
            case AUTH:
                logger.info("socks server auth");
                ctx.pipeline().addFirst(new SocksCmdRequestDecoder());
                ctx.write(new SocksAuthResponse(SocksAuthStatus.SUCCESS));
                break;
            case CMD:
                SocksCmdRequest req = (SocksCmdRequest) msg;
                if (req.cmdType() == SocksCmdType.CONNECT) {
                    logger.info("socks server connect");
                    Channel socksChannel = ctx.channel();
                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(socksChannel.eventLoop())
                            .channel(ctx.channel().getClass())
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                            .option(ChannelOption.SO_KEEPALIVE, true)
                            .handler(new ProxyRemoteHandler(socksChannel));

                    ChannelFuture channelFuture = bootstrap.connect(remoteHost, remotePort);

                    Channel remoteChannel = channelFuture.channel();
                    socksChannel.pipeline().addLast(new ProxySocksHandler(remoteChannel));

                    channelFuture.addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            socksChannel.writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS, req.addressType()))
                                    .addListener((ChannelFutureListener) future1 -> {
                                        if (future.isSuccess()) {
                                            /*ByteBuf buff = Unpooled.buffer();
                                            req.encodeAsByteBuf(buff);
                                            remoteChannel.writeAndFlush(buff.skipBytes(10));*/
                                        } else {
                                            remoteChannel.close();
                                        }
                                    });
                        } else {
                            socksChannel.close();
                        }
                    });

                    socksChannel.pipeline().remove(this);
                } else {
                    ctx.close();
                }
                break;
            case UNKNOWN:
                ctx.close();
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }
}
