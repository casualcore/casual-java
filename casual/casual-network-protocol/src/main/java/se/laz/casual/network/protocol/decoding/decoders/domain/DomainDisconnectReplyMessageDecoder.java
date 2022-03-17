/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.domain;

import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.domain.DomainDisconnectReplyMessage;
import se.laz.casual.network.protocol.messages.parseinfo.ConnectRequestSizes;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.UUID;

public final class DomainDisconnectReplyMessageDecoder implements NetworkDecoder<DomainDisconnectReplyMessage>
{
    private DomainDisconnectReplyMessageDecoder()
    {}

    public static NetworkDecoder<DomainDisconnectReplyMessage> of()
    {
        return new DomainDisconnectReplyMessageDecoder();
    }

    @Override
    public DomainDisconnectReplyMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        return getMessage(b.array());
    }

    @Override
    public DomainDisconnectReplyMessage readChunked(final ReadableByteChannel channel)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(channel);
        return DomainDisconnectReplyMessage.of(execution);
    }

    @Override
    public DomainDisconnectReplyMessage readSingleBuffer(byte[] data)
    {
        return getMessage(data);
    }

    private DomainDisconnectReplyMessage getMessage(final byte[] bytes)
    {
        int currentOffset = 0;
        final UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, ConnectRequestSizes.EXECUTION.getNetworkSize()));
        return DomainDisconnectReplyMessage.of(execution);
    }

}
