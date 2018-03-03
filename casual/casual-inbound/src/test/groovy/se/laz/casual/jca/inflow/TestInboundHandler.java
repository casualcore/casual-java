/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;

public class TestInboundHandler extends SimpleChannelInboundHandler<CasualNWMessage<?>>
{
    private CasualNWMessage<?> msg;
    private TestInboundHandler()
    {}

    public static TestInboundHandler of()
    {
        return new TestInboundHandler();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CasualNWMessage<?> msg) throws Exception
    {
        this.msg = msg;
    }
    public CasualNWMessage<?> getMsg()
    {
        return msg;
    }
}
