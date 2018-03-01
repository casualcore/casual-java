package se.kodarkatten.casual.network.protocol.decoding

import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage
import se.kodarkatten.casual.api.network.protocol.messages.CasualNetworkTransmittable
import se.kodarkatten.casual.network.protocol.decoding.decoders.MessageDecoder
import se.kodarkatten.casual.network.protocol.decoding.decoders.NetworkDecoder
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageHeader
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.messages.parseinfo.MessageHeaderSizes
import se.kodarkatten.casual.network.protocol.utils.ByteUtils
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel

class CasualNetworkTestReader
{
    static CasualNWMessageHeader networkHeaderToCasualHeader(final ReadableByteChannel channel )
    {
        final ByteBuffer headerBuffer = ByteUtils.readFully(channel, MessageHeaderSizes.getHeaderNetworkSize())
        return CasualMessageDecoder.networkHeaderToCasualHeader(headerBuffer.array())
    }

    static <T extends CasualNetworkTransmittable> CasualNWMessage<T> read(final ReadableByteChannel channel, CasualNWMessageHeader header )
    {
        NetworkDecoder<T> networkReader = CasualMessageDecoder.getDecoder( header )
        return readMessage( channel, header, networkReader )
    }

    static <T extends CasualNetworkTransmittable> CasualNWMessage<T> read(final ReadableByteChannel channel)
    {
        final CasualNWMessageHeader header = networkHeaderToCasualHeader( channel )
        return read( channel, header )
    }

    static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readMessage(final ReadableByteChannel channel, final CasualNWMessageHeader header, NetworkDecoder<T> nr )
    {
        final MessageDecoder<T> reader = MessageDecoder.of(nr, CasualMessageDecoder.getMaxSingleBufferByteSize() )
        final T msg = reader.read(channel, header.getPayloadSize())
        return CasualNWMessageImpl.of(header.getCorrelationId(), msg)
    }

}
