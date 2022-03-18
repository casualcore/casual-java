/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;

import java.util.Objects;
import java.util.logging.Logger;

public class CasualMessageHandler extends SimpleChannelInboundHandler<CasualNWMessage<?>>
{
    private static final Logger LOG = Logger.getLogger(CasualMessageHandler.class.getName());
    private final Correlator correlator;

    private CasualMessageHandler(final Correlator correlator)
    {
        this.correlator = correlator;
    }

    public static CasualMessageHandler of(final Correlator correlator)
    {
        Objects.requireNonNull(correlator, "correlator can not be null");
        return new CasualMessageHandler(correlator);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final CasualNWMessage<?> msg)
    {
        LOG.finest(() -> String.format("reply: %s", LogTool.asLogEntry(msg)));
        if(isConversationalMessage(msg.getType()))
        {
            // pass along the pipeline to the next handler
            ctx.fireChannelRead(msg);
            return;
        }
        correlator.complete(msg);
    }

    private boolean isConversationalMessage(CasualNWMessageType type)
    {
        return type == CasualNWMessageType.CONVERSATION_REQUEST;
    }

}
