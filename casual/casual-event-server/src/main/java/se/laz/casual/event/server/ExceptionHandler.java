/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;

import java.util.logging.Logger;

@ChannelHandler.Sharable
public final class ExceptionHandler extends ChannelInboundHandlerAdapter
{
    private static final Logger log = Logger.getLogger(ExceptionHandler.class.getName());
    private final ChannelGroup connectedClients;

    public ExceptionHandler(ChannelGroup connectedClients)
    {
        this.connectedClients = connectedClients;
    }

    public static ExceptionHandler of(ChannelGroup connectedClients)
    {
        return new ExceptionHandler(connectedClients);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        log.warning(() -> "EventServer exception caught: " + cause + " closing channel: " + ctx.channel());
        connectedClients.remove(ctx.channel());
        ctx.close();
    }
}
