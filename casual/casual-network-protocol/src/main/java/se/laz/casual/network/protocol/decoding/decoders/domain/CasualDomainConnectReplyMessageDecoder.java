/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.domain;

import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.decoding.decoders.utils.DecoderReaderValidator;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.messages.parseinfo.ConnectReplySizes;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public final class CasualDomainConnectReplyMessageDecoder implements NetworkDecoder<CasualDomainConnectReplyMessage>
{
    private CasualDomainConnectReplyMessageDecoder()
    {}

    public static NetworkDecoder<CasualDomainConnectReplyMessage> of()
    {
        return new CasualDomainConnectReplyMessageDecoder();
    }

    @Override
    public CasualDomainConnectReplyMessage readSingleBuffer(byte[] data)
    {
        return getMessage(data);
    }

    private CasualDomainConnectReplyMessage getMessage(final byte[] bytes)
    {
        int currentOffset = 0;
        final UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, CommonSizes.EXECUTION.getNetworkSize()));
        currentOffset +=  CommonSizes.EXECUTION.getNetworkSize();
        final UUID domainId = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, currentOffset + ConnectReplySizes.DOMAIN_ID.getNetworkSize()));
        currentOffset += ConnectReplySizes.DOMAIN_ID.getNetworkSize();
        final int domainNameSize = (int)ByteBuffer.wrap(bytes, currentOffset , ConnectReplySizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ConnectReplySizes.DOMAIN_NAME_SIZE.getNetworkSize();
        final String domainName = CasualMessageDecoderUtils.getAsString(bytes, currentOffset, domainNameSize);
        currentOffset += domainNameSize;
        long protocol = ByteBuffer.wrap(bytes, currentOffset, ConnectReplySizes.PROTOCOL_VERSION_SIZE.getNetworkSize()).getLong();
        currentOffset += ConnectReplySizes.PROTOCOL_VERSION_SIZE.getNetworkSize();

        DecoderReaderValidator.throwIfDataNotFullyRead( currentOffset, bytes.length );

        return CasualDomainConnectReplyMessage.createBuilder()
                                              .withExecution(execution)
                                              .withDomainId(domainId)
                                              .withDomainName(domainName)
                                              .withProtocolVersion(protocol)
                                              .build();
    }

}
