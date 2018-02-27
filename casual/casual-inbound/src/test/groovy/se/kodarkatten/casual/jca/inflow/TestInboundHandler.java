package se.kodarkatten.casual.jca.inflow;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage;

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
