/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.network.inbound.ProtocolVersionValueHolder;
import se.laz.casual.network.protocol.decoding.CasualMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.CasualNWMessageHeaderDecoder;
import se.laz.casual.network.protocol.messages.CasualNWMessageHeader;
import se.laz.casual.network.protocol.messages.parseinfo.MessageHeaderSizes;

import java.util.List;
import java.util.Optional;

public class CasualNWMessageDecoder extends ByteToMessageDecoder
{
    private final ProtocolVersionValueHolder protocolVersionValueHolder;
    private enum State{
        READ_HEADER, READ_PAYLOAD
    }
    private CasualNWMessageHeader header;
    private State state = State.READ_HEADER;
    private CasualNWMessageDecoder(ProtocolVersionValueHolder protocolVersionValueHolder)
    {
        this.protocolVersionValueHolder = protocolVersionValueHolder;
    }
    public static CasualNWMessageDecoder of(ProtocolVersionValueHolder protocolVersionValueHolder)
    {
        return new CasualNWMessageDecoder(protocolVersionValueHolder);
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out)
    {
        if(state == State.READ_HEADER )
        {
            readHeader(in);
            return;
        }
        readPayload( in ).ifPresent(out::add);
    }

    /**
     * Wait until enough bytes available to read header fully before reading the payload.
     */
    private void readHeader( final ByteBuf in )
    {
        if(in.readableBytes() < MessageHeaderSizes.getHeaderNetworkSize())
        {
            return;
        }
        byte[] headerBytes = new byte[MessageHeaderSizes.getHeaderNetworkSize()];
        in.readBytes(headerBytes);
        header = CasualNWMessageHeaderDecoder.fromNetworkBytes(headerBytes);
        state = State.READ_PAYLOAD;
    }

    /**
     * Wait until enough bytes available to read payload fully. Then return the message.
     */
    private Optional<CasualNWMessage<?>> readPayload(final ByteBuf in )
    {
        if(in.readableBytes() < header.getPayloadSize())
        {
            return Optional.empty();
        }
        try
        {
            byte[] messageBytes = new byte[(int)header.getPayloadSize()];
            in.readBytes(messageBytes);
            return Optional.of( CasualMessageDecoder.read(messageBytes, header, protocolVersionValueHolder) );
        }
        catch(Exception e)
        {
            throw new CasualDecoderException(e, header.getCorrelationId());
        }
        finally
        {
            state = State.READ_HEADER;
        }
    }
}
