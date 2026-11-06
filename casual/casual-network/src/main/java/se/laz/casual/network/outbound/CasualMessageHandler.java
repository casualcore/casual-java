/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.jca.DomainId;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage;

import java.util.Objects;
import java.util.logging.Logger;

public class CasualMessageHandler extends SimpleChannelInboundHandler<CasualNWMessage<?>>
{
    private static final Logger LOG = Logger.getLogger(CasualMessageHandler.class.getName());
    private final Correlator correlator;

    private CasualOutboundMessageListener messageListener;

    private CasualMessageHandler(final Correlator correlator)
    {
        this.correlator = correlator;
    }

    public static CasualMessageHandler of(final Correlator correlator)
    {
        Objects.requireNonNull(correlator, "correlator can not be null");
        return new CasualMessageHandler(correlator);
    }

    public void setMessageListener(CasualOutboundMessageListener messageListener)
    {
        Objects.requireNonNull(messageListener, "messageListener can not be null");
        this.messageListener = messageListener;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final CasualNWMessage<?> msg)
    {
        LOG.finest(() -> String.format("reply: %s", LogTool.asLogEntry(msg)));
        if(msg.getMessage() instanceof CasualDomainConnectReplyMessage connectReply)
        {
            // The decoder has already stored the negotiated protocol version. Initialize the peer
            // domain here before the correlator releases the handshake thread. Otherwise, the event
            // loop could process a following disconnect or topology update before the connection
            // has its peer domain ID.
            messageListener.domainConnected(DomainId.of(connectReply.getDomainId()));
        }
        if(isConversationalMessage(msg.getType()))
        {
            // pass along the pipeline to the next handler
            ctx.fireChannelRead(msg);
            return;
        }
        if(messageListener.isInterestedIn(msg.getType()))
        {
            // For messages that does not originate from our side
            // For instance, topology changed and domain disconnect
            // Since we did not initiate - we can not correlate
            messageListener.handleMessage(msg);
            return;
        }
        correlator.complete(msg);
    }

    private boolean isConversationalMessage(CasualNWMessageType type)
    {
        return type == CasualNWMessageType.CONVERSATION_REQUEST;
    }
}
