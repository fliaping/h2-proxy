package com.fliaping.proxy.h2.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

public class NettyMQTTHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(NettyMQTTHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MqttMessage mqttMessage = (MqttMessage) msg;
        MqttMessageType messageType = mqttMessage.fixedHeader().messageType();
        logger.info("receive messageType:{}", messageType);
        switch (messageType) {

            case CONNECT:
                MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE,
                        false, 0);
                MqttConnAckVariableHeader mqttConnAckVariableHeader = new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, true);
                MqttConnAckMessage ackMessage = new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
                ctx.writeAndFlush(ackMessage);
                break;
            case SUBSCRIBE:

                break;
            case UNSUBSCRIBE:

                break;
            case PUBLISH:

                break;
            case PUBREC:

                break;
            case PUBCOMP:

                break;
            case PUBREL:

                break;
            case DISCONNECT:

                break;
            case PUBACK:

                break;
            case PINGREQ:
                MqttFixedHeader pingHeader = new MqttFixedHeader(
                        MqttMessageType.PINGRESP,
                        false,
                        AT_MOST_ONCE,
                        false,
                        0);
                MqttMessage pingResp = new MqttMessage(pingHeader);
                ctx.writeAndFlush(pingResp);
                break;
            default:
                logger.error("Unkonwn MessageType:{}", messageType);
                break;
        }
    }
}
