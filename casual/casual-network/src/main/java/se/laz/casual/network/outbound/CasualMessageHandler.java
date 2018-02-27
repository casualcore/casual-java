package se.laz.casual.network.outbound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage;

import java.util.Objects;

public final class CasualMessageHandler extends SimpleChannelInboundHandler<CasualNWMessage<?>>
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
