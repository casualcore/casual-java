package se.laz.casual.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage;
import se.kodarkatten.casual.network.protocol.io.CasualNetworkReader;
import se.kodarkatten.casual.network.protocol.io.readers.CasualNWMessageHeaderReader;
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageHeader;
import se.kodarkatten.casual.network.protocol.messages.exceptions.CasualProtocolException;
import se.kodarkatten.casual.network.protocol.messages.parseinfo.MessageHeaderSizes;

import java.util.List;

public final class CasualNWMessageDecoder extends ByteToMessageDecoder
{
    private enum State{
        READ_HEADER, READ_PAYLOAD
    }
    private CasualNWMessageHeader header;
    private State state = State.READ_HEADER;
    private CasualNWMessageDecoder()
    {}
    public static CasualNWMessageDecoder of()
    {
        return new CasualNWMessageDecoder();
    }
    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out)
    {
        switch(state)
        {
            case READ_HEADER:
                if(in.readableBytes() < MessageHeaderSizes.getHeaderNetworkSize())
                {
                    return;
                }
                byte[] headerBytes = new byte[MessageHeaderSizes.getHeaderNetworkSize()];
                in.readBytes(headerBytes);
                header = CasualNWMessageHeaderReader.fromNetworkBytes(headerBytes);
                state = State.READ_PAYLOAD;
                break;
            case READ_PAYLOAD:
                if(in.readableBytes() < header.getPayloadSize())
                {
                    return;
                }
                byte[] messageBytes = new byte[(int)header.getPayloadSize()];
                try
                {
                    in.readBytes(messageBytes);
                    CasualNWMessage<?> msg = CasualNetworkReader.read(messageBytes, header);
                    out.add(msg);
                    state = State.READ_HEADER;
                }
                catch(Throwable e)
                {
                    throw new CasualDecoderException(e, header.getCorrelationId());
                }
                break;
        }
    }
}
