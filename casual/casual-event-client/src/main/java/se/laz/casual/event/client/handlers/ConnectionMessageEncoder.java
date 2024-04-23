/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.client.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import se.laz.casual.event.client.messages.ConnectionMessage;

import java.nio.charset.StandardCharsets;

public class ConnectionMessageEncoder extends MessageToByteEncoder<ConnectionMessage>
{
    private ConnectionMessageEncoder()
    {}

    public static ConnectionMessageEncoder of()
    {
        return new ConnectionMessageEncoder();
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ConnectionMessage connectionMessage, ByteBuf byteBuf)
    {
        byteBuf.writeBytes(connectionMessage.getConnectionMessage().getBytes(StandardCharsets.UTF_8));
    }
    @Override
    public boolean acceptOutboundMessage(Object msg)
    {
        return msg instanceof ConnectionMessage;
    }
}
