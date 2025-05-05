/*
 * Copyright (c) 2024 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound.reverse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import se.laz.casual.jca.inflow.CasualInboundTransactionRegistry;
import se.laz.casual.network.InboundDeactivatedContext;
import se.laz.casual.network.InboundTopologyUpdateContext;

import java.util.Objects;
import java.util.logging.Logger;

public final class ReverseInboundExceptionHandler extends ChannelInboundHandlerAdapter
{
    private static final Logger log = Logger.getLogger(ReverseInboundExceptionHandler.class.getName());
    private final CasualInboundTransactionRegistry inboundTransactionRegistry;

    private ReverseInboundExceptionHandler(CasualInboundTransactionRegistry inboundTransactionRegistry)
    {
        this.inboundTransactionRegistry = inboundTransactionRegistry;
    }

    public static ReverseInboundExceptionHandler of(CasualInboundTransactionRegistry inboundTransactionRegistry)
    {
        Objects.requireNonNull(inboundTransactionRegistry, "inboundTransactionRegistry can not be null");
        return new ReverseInboundExceptionHandler(inboundTransactionRegistry);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        log.warning(() -> "casual reverse inbound exception caught: " + cause + " closing channel");
        InboundDeactivatedContext.remove(ctx.channel());
        InboundTopologyUpdateContext.remove(ctx.channel());
        inboundTransactionRegistry.remove(ctx.channel().id());
        ctx.close();
    }
}
