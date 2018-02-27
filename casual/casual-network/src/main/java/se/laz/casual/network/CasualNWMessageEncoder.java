package se.laz.casual.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage;
import se.kodarkatten.casual.network.protocol.io.CasualNetworkWriter;

public final class CasualNWMessageEncoder extends MessageToByteEncoder<CasualNWMessage<?>>
{
    private CasualNWMessageEncoder()
    {}
    public static CasualNWMessageEncoder of()
    {
        return new CasualNWMessageEncoder();
    }
    @Override
    protected void encode(ChannelHandlerContext ctx, CasualNWMessage<?> msg, ByteBuf out) throws Exception
    {
        CasualNetworkWriter.write(out, msg);
    }

    @Override
    public boolean acceptOutboundMessage(Object msg)
    {
        return msg instanceof CasualNWMessage;
    }

}
