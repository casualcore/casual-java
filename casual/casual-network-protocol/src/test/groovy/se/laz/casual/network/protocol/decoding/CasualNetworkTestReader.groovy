/*
 * Copyright (c) 2017 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding

import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.protocol.decoding.decoders.MessageDecoder
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder
import se.laz.casual.network.protocol.messages.CasualNWMessageHeader
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.parseinfo.MessageHeaderSizes
import se.laz.casual.network.protocol.utils.ByteUtils

import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import java.util.function.Supplier;

class CasualNetworkTestReader
{
    static CasualNWMessageHeader networkHeaderToCasualHeader(final ReadableByteChannel channel )
    {
        final ByteBuffer headerBuffer = ByteUtils.readFully(channel, MessageHeaderSizes.getHeaderNetworkSize())
        return CasualMessageDecoder.networkHeaderToCasualHeader(headerBuffer.array())
    }

   static <T extends CasualNetworkTransmittable> CasualNWMessage<T> read(final ReadableByteChannel channel, CasualNWMessageHeader header)
   {
      return read(channel, header, ProtocolVersion.VERSION_1_0)
   }

    static <T extends CasualNetworkTransmittable> CasualNWMessage<T> read(final ReadableByteChannel channel, CasualNWMessageHeader header, ProtocolVersion protocolVersion )
    {
        Supplier<ProtocolVersion> protocolVersionSupplier = {protocolVersion}
        NetworkDecoder<T> networkReader = CasualMessageDecoder.getDecoder(header.getType(), protocolVersionSupplier)
        return readMessage( channel, header, networkReader )
    }

    static <T extends CasualNetworkTransmittable> CasualNWMessage<T> read(final ReadableByteChannel channel)
    {
       return read(channel, ProtocolVersion.VERSION_1_0)
    }

    static <T extends CasualNetworkTransmittable> CasualNWMessage<T> read(final ReadableByteChannel channel, ProtocolVersion protocolVersion)
    {
        final CasualNWMessageHeader header = networkHeaderToCasualHeader( channel )
        return read( channel, header, protocolVersion)
    }

    static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readMessage(final ReadableByteChannel channel, final CasualNWMessageHeader header, NetworkDecoder<T> nr )
    {
        final MessageDecoder<T> reader = MessageDecoder.of(nr, CasualMessageDecoder.getMaxSingleBufferByteSize() )
        final T msg = reader.read(channel, header.getPayloadSize())
        return CasualNWMessageImpl.of(header.getCorrelationId(), msg)
    }

    static <T extends CasualNetworkTransmittable> T readMessage(CasualNWMessageType type, ReadableByteChannel channel, long messageSize, ProtocolVersion protocolVersion)
    {
       NetworkDecoder<T> networkReader = CasualMessageDecoder.getDecoder(type, () -> protocolVersion)
       final MessageDecoder<T> reader = MessageDecoder.of(networkReader, CasualMessageDecoder.getMaxSingleBufferByteSize() )
       return reader.read(channel, messageSize)
    }

}
