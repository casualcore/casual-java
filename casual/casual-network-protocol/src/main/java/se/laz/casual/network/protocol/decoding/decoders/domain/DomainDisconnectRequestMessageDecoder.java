/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.domain;

import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.domain.DomainDisconnectRequestMessage;
import se.laz.casual.network.protocol.messages.parseinfo.ConnectRequestSizes;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.UUID;

public final class DomainDisconnectRequestMessageDecoder implements NetworkDecoder<DomainDisconnectRequestMessage>
{
    private DomainDisconnectRequestMessageDecoder()
    {}

    public static NetworkDecoder<DomainDisconnectRequestMessage> of()
    {
        return new DomainDisconnectRequestMessageDecoder();
    }

    @Override
    public DomainDisconnectRequestMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        return getMessage(b.array());
    }

    @Override
    public DomainDisconnectRequestMessage readChunked(final ReadableByteChannel channel)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(channel);
        return DomainDisconnectRequestMessage.of(execution);
    }

    @Override
    public DomainDisconnectRequestMessage readSingleBuffer(byte[] data)
    {
        return getMessage(data);
    }

    private DomainDisconnectRequestMessage getMessage(final byte[] bytes)
    {
        int currentOffset = 0;
        final UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, ConnectRequestSizes.EXECUTION.getNetworkSize()));
        return DomainDisconnectRequestMessage.of(execution);
    }

}
