/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import se.laz.casual.api.external.json.JsonProviderFactory;
import se.laz.casual.event.server.messages.ConnectReplyMessage;

import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class ConnectReplyMessageEncoder extends MessageToByteEncoder<ConnectReplyMessage>
{
    public static ConnectReplyMessageEncoder of()
    {
        return new ConnectReplyMessageEncoder();
    }
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ConnectReplyMessage connectReplyMessage, ByteBuf byteBuf) throws Exception
    {
        String jsonPayload = JsonProviderFactory.getJsonProvider().toJson(connectReplyMessage);
        byteBuf.writeBytes(jsonPayload.getBytes(StandardCharsets.UTF_8));
    }
}
