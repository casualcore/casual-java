/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import se.laz.casual.network.messages.CasualReply;

public class TestInboundHandler extends SimpleChannelInboundHandler<CasualReply>
{
    private CasualReply msg;
    private TestInboundHandler()
    {}

    public static TestInboundHandler of()
    {
        return new TestInboundHandler();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CasualReply msg) throws Exception
    {
        this.msg = msg;
    }
    public CasualReply getMsg()
    {
        return msg;
    }
}
