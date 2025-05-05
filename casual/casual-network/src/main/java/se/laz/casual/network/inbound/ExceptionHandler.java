/*
 * Copyright (c) 2017 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import se.laz.casual.jca.inflow.CasualInboundTransactionRegistry;
import se.laz.casual.network.InboundDeactivatedContext;
import se.laz.casual.network.InboundTopologyUpdateContext;

import java.util.Objects;
import java.util.logging.Logger;

@ChannelHandler.Sharable
public final class ExceptionHandler extends ChannelInboundHandlerAdapter
{
    private static final Logger log = Logger.getLogger(ExceptionHandler.class.getName());
    private final CasualInboundTransactionRegistry inboundTransactionRegistry;

    public ExceptionHandler(CasualInboundTransactionRegistry inboundTransactionRegistry)
    {
        this.inboundTransactionRegistry = inboundTransactionRegistry;
    }

    public static ExceptionHandler of(CasualInboundTransactionRegistry inboundTransactionRegistry)
    {
        Objects.requireNonNull(inboundTransactionRegistry, "inboundTransactionRegistry can not be null");
        return new ExceptionHandler(inboundTransactionRegistry);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        log.warning(() -> "casual inbound exception caught: " + cause + " closing channel");
        InboundDeactivatedContext.remove(ctx.channel());
        InboundTopologyUpdateContext.remove(ctx.channel());
        inboundTransactionRegistry.remove(ctx.channel().id());
        ctx.close();
    }
}
