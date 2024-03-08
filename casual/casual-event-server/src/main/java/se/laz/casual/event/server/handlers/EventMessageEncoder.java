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
import se.laz.casual.event.ServiceCallEvent;

import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class EventMessageEncoder extends MessageToByteEncoder<ServiceCallEvent>
{
    private EventMessageEncoder()
    {}
    @Override
    protected void encode(ChannelHandlerContext ctx, ServiceCallEvent msg, ByteBuf out) throws Exception
    {
        String jsonPayload = JsonProviderFactory.getJsonProvider().toJson(msg);
        out.writeBytes(jsonPayload.getBytes(StandardCharsets.UTF_8));
    }
    public static EventMessageEncoder of()
    {
        return new EventMessageEncoder();
    }
}
