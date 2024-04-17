/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.CharsetUtil;
import se.laz.casual.api.external.json.JsonProviderFactory;
import se.laz.casual.event.server.ConnectionReplier;
import se.laz.casual.event.server.messages.ConnectRequestMessage;
import se.laz.casual.event.server.messages.ConnectRequestMessageTypeAdapter;

import java.util.Objects;
import java.util.logging.Logger;

@ChannelHandler.Sharable
public class FromJSONConnectDecoder extends SimpleChannelInboundHandler<Object>
{
    private static final Logger log = Logger.getLogger(FromJSONConnectDecoder.class.getName());
    private static final ConnectionReplier CONNECTION_REPLIER = ConnectionReplier.of();
    private final ChannelGroup connectedClients;

    private FromJSONConnectDecoder(ChannelGroup connectedClients)
    {
        this.connectedClients = connectedClients;
    }

    public static FromJSONConnectDecoder of(ChannelGroup connectedClients)
    {
        Objects.requireNonNull(connectedClients, "connectedClients can not be null");
        return new FromJSONConnectDecoder(connectedClients);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        ByteBuf content = (ByteBuf)msg;
        String json = content.toString(CharsetUtil.UTF_8);
        ConnectRequestMessage requestMessage = JsonProviderFactory.getJsonProvider().fromJson(json, ConnectRequestMessage.class, ConnectRequestMessageTypeAdapter.of());
        connectedClients.add(ctx.channel());
        ctx.fireChannelRead(requestMessage);
        log.finest(() -> "EventServer, client logged on: " + requestMessage + " channel: " + ctx.channel());
        CONNECTION_REPLIER.clientConnected(ctx.channel());
    }
}
