/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import se.laz.casual.network.messages.CasualReply;

import java.util.Objects;

public final class CasualMessageHandler extends SimpleChannelInboundHandler<CasualReply>
{
    private final Correlator correlator;

    private CasualMessageHandler(boolean autoRelease, Correlator correlator)
    {
        super(autoRelease);
        this.correlator = correlator;
    }

    public static CasualMessageHandler of(Correlator correlator)
    {
        Objects.requireNonNull(correlator, "correlator can not be null");
        return new CasualMessageHandler(true, correlator);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CasualReply msg) throws Exception
    {
        correlator.complete(msg);
    }

}
