package com.fliaping.proxy.h2.local;

import io.netty.channel.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.fliaping.proxy.h2.local.ChannelUtils.closeOnFlush;

public class ProxyRemoteHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(ProxyRemoteHandler.class);

    private Channel socksChannel;

    public ProxyRemoteHandler(Channel socksChannel) {
        this.socksChannel = socksChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
        ctx.pipeline().addFirst(new LoggingHandler(LogLevel.INFO));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            logger.debug("get remote message:{}", socksChannel);
            if (socksChannel.isActive()) {
                socksChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                });
                /*if (!byteBuf.hasArray()) {
                    int len = byteBuf.readableBytes();
                    byte[] arr = new byte[len];
                    byteBuf.getBytes(0, arr);
                }*/
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {
           // ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        closeOnFlush(socksChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }
}
