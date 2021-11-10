/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;

import java.util.Objects;

public class CasualMessageHandler extends SimpleChannelInboundHandler<CasualNWMessage<?>>
{
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
        correlator.complete(msg);
    }

}
