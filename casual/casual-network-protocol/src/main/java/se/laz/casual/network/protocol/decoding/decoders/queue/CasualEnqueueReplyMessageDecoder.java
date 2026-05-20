/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.queue;

import se.laz.casual.api.queue.QueueErrorCode;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.decoding.decoders.utils.DecoderReaderValidator;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.messages.queue.CasualEnqueueReplyMessage;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

import static se.laz.casual.network.ProtocolVersion.VERSION_1_3;

public class CasualEnqueueReplyMessageDecoder implements NetworkDecoder<CasualEnqueueReplyMessage>
{
    private final ProtocolVersion protocolVersion;

    private CasualEnqueueReplyMessageDecoder(ProtocolVersion protocolVersion)
    {
        this.protocolVersion = protocolVersion;
    }

    public static CasualEnqueueReplyMessageDecoder of(ProtocolVersion protocolVersion)
    {
        return new CasualEnqueueReplyMessageDecoder(protocolVersion);
    }

    @Override
    public CasualEnqueueReplyMessage readSingleBuffer(byte[] data)
    {
        return getMessage(data, protocolVersion);
    }

    private static CasualEnqueueReplyMessage getMessage(final byte[] bytes, ProtocolVersion protocolVersion)
    {
        int currentOffset = 0;
        UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, CommonSizes.EXECUTION.getNetworkSize()));
        currentOffset += CommonSizes.EXECUTION.getNetworkSize();
        UUID id = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, currentOffset + CommonSizes.EXECUTION.getNetworkSize()));
        currentOffset += CommonSizes.UUID_ID.getNetworkSize();
        CasualEnqueueReplyMessage.Builder builder = CasualEnqueueReplyMessage.createBuilder()
                                                                             .withExecution(execution)
                                                                             .withId(id);
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ) )
        {
            final ByteBuffer callErrorBuffer = ByteBuffer.wrap(bytes, currentOffset, CommonSizes.CALL_ERROR.getNetworkSize());
            int callError = callErrorBuffer.getInt();
            builder.withCode(QueueErrorCode.unmarshal(callError));
            currentOffset += CommonSizes.CALL_ERROR.getNetworkSize();
        }

        DecoderReaderValidator.throwIfDataNotFullyRead( currentOffset, bytes.length );

        builder.withProtocolVersion(protocolVersion);
        return builder.build();
    }

}
