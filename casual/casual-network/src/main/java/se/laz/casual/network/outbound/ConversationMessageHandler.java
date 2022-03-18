/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.network.protocol.messages.conversation.Request;

import java.util.Objects;

public class ConversationMessageHandler extends SimpleChannelInboundHandler<CasualNWMessage<Request>>
{
    private final ConversationMessageStorage storage;

    private ConversationMessageHandler(ConversationMessageStorage storage)
    {
        this.storage = storage;
    }

    public static ConversationMessageHandler of(final ConversationMessageStorage storage)
    {
        Objects.requireNonNull(storage, "storage can not be null");
        return new ConversationMessageHandler(storage);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CasualNWMessage<Request> msg) throws Exception
    {
        storage.put(msg.getCorrelationId(), msg);
    }
}
