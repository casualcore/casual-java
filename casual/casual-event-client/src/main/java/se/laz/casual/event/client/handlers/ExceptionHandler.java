/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.client.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionHandler extends ChannelInboundHandlerAdapter
{
    private static final Logger LOG = Logger.getLogger(ExceptionHandler.class.getName());
    public static ExceptionHandler of()
    {
        return new ExceptionHandler();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        LOG.log(Level.WARNING, cause, () -> String.format("Exception caught %s - closing connection", cause.getMessage()));
        ctx.close();
    }
}
