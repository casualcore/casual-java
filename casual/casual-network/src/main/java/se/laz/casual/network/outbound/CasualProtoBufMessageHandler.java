package se.laz.casual.network.outbound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import se.laz.casual.network.messages.CasualReply;

import java.util.Objects;

public class CasualProtoBufMessageHandler extends SimpleChannelInboundHandler<CasualReply>
{
    private final Correlator correlator;

    private CasualProtoBufMessageHandler(boolean autoRelease, Correlator correlator)
    {
        super(autoRelease);
        this.correlator = correlator;
    }

    public static CasualProtoBufMessageHandler of(Correlator correlator)
    {
        Objects.requireNonNull(correlator, "correlator can not be null");
        return new CasualProtoBufMessageHandler(true, correlator);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CasualReply msg) throws Exception
    {
        correlator.complete(msg);
    }
}
