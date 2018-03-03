/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Logger;

@ChannelHandler.Sharable
public final class ExceptionHandler extends ChannelInboundHandlerAdapter
{
    private static final Logger log = Logger.getLogger(ChannelInboundHandlerAdapter.class.getName());
    public static ExceptionHandler of()
    {
        return new ExceptionHandler();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        log.warning(() -> "casual inbound exception caught: " + cause + " closing channel");
        ctx.close();
    }
}
