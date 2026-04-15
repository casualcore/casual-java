/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.domain;

import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.domain.DomainDisconnectReplyMessage;
import se.laz.casual.network.protocol.messages.parseinfo.ConnectRequestSizes;

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
