/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.server.handlers;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;

@ChannelHandler.Sharable
public final class ExceptionHandler extends ChannelInboundHandlerAdapter
{
    private static final System.Logger log = System.getLogger(ExceptionHandler.class.getName());
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
        log.log(System.Logger.Level.WARNING,() -> "EventServer exception caught: " + cause + " closing channel: " + ctx.channel());
        connectedClients.remove(ctx.channel());
        ctx.close();
    }
}
