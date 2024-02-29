/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound.reverse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Logger;

public final class ReverseInboundExceptionHandler extends ChannelInboundHandlerAdapter
{
    private static final Logger log = Logger.getLogger(ReverseInboundExceptionHandler.class.getName());
    public static ReverseInboundExceptionHandler of()
    {
        return new ReverseInboundExceptionHandler();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        log.warning(() -> "casual reverse inbound exception caught: " + cause + " closing channel");
        ctx.close();
    }
}
