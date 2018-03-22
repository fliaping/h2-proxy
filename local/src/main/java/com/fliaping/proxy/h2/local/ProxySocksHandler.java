package com.fliaping.proxy.h2.local;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.fliaping.proxy.h2.local.ChannelUtils.closeOnFlush;

public class ProxySocksHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(ProxySocksHandler.class);


    private Channel remoteChannel;

    public ProxySocksHandler(Channel remoteChannel) {
        this.remoteChannel = remoteChannel;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            logger.debug("get socks message:{}", remoteChannel);
            if (remoteChannel.isActive()) {
                ByteBuf byteBuf  = (ByteBuf) msg;
                remoteChannel.writeAndFlush(byteBuf)
                        .addListener((ChannelFutureListener) future -> {
                            if (future.isSuccess()) {
                                ctx.channel().read();
                            } else {
                                future.channel().close();
                            }
                        });
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {
           // ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        closeOnFlush(remoteChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }


}
