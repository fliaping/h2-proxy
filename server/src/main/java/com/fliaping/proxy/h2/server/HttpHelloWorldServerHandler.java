package com.fliaping.proxy.h2.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;

public class HttpHelloWorldServerHandler extends ChannelInboundHandlerAdapter {

    private static final byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' };

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;

            boolean keepAlive = HttpUtil.isKeepAlive(request);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(CONTENT));

            if (!keepAlive) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain");
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                response.headers().set(HttpHeaderNames.CONNECTION, KEEP_ALIVE);
                ctx.write(response);
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
