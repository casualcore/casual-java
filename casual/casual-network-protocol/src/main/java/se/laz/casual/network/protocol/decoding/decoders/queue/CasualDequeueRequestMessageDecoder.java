/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.queue;

import se.laz.casual.api.util.Pair;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.decoding.decoders.utils.DecoderReaderValidator;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.messages.parseinfo.DequeueRequestSizes;
import se.laz.casual.network.protocol.messages.queue.CasualDequeueRequestMessage;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public final class CasualDequeueRequestMessageDecoder implements NetworkDecoder<CasualDequeueRequestMessage>
{
    private CasualDequeueRequestMessageDecoder()
    {}

    public static CasualDequeueRequestMessageDecoder of()
    {
        return new CasualDequeueRequestMessageDecoder();
    }

    @Override
    public CasualDequeueRequestMessage readSingleBuffer(byte[] data)
    {
        return getMessage(data);
    }

    private static CasualDequeueRequestMessage getMessage(final byte[] bytes)
    {
        int currentOffset = 0;
        UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, CommonSizes.EXECUTION.getNetworkSize()));
        currentOffset += CommonSizes.EXECUTION.getNetworkSize();
        int queueNameSize = (int)ByteBuffer.wrap(bytes, currentOffset , DequeueRequestSizes.NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += DequeueRequestSizes.NAME_SIZE.getNetworkSize();
        String queueName = CasualMessageDecoderUtils.getAsString(bytes, currentOffset, queueNameSize);
        currentOffset += queueNameSize;
        Pair<Integer, Xid> xidInfo = CasualMessageDecoderUtils.readXid(bytes, currentOffset);
        currentOffset = xidInfo.first();
        Xid xid = xidInfo.second();
        int selectorPropertiesSize = (int)ByteBuffer.wrap(bytes, currentOffset , DequeueRequestSizes.SELECTOR_PROPERTIES_SIZE.getNetworkSize()).getLong();
        currentOffset += DequeueRequestSizes.SELECTOR_PROPERTIES_SIZE.getNetworkSize();
        String selectorProperties = (0 == selectorPropertiesSize) ? "" : CasualMessageDecoderUtils.getAsString(bytes, currentOffset, selectorPropertiesSize);
        currentOffset += selectorPropertiesSize;
        UUID selectorId = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, currentOffset + DequeueRequestSizes.SELECTOR_ID_SIZE.getNetworkSize()));
        currentOffset += CommonSizes.EXECUTION.getNetworkSize();
        boolean block = (1 == ByteBuffer.wrap(bytes, currentOffset , DequeueRequestSizes.BLOCK.getNetworkSize()).get());
        currentOffset += DequeueRequestSizes.BLOCK.getNetworkSize();

        DecoderReaderValidator.throwIfDataNotFullyRead( currentOffset, bytes.length );

        return CasualDequeueRequestMessage.createBuilder()
                                          .withExecution(execution)
                                          .withQueueName(queueName)
                                          .withXid(xid)
                                          .withSelectorProperties(selectorProperties)
                                          .withSelectorUUID(selectorId)
                                          .withBlock(block)
                                          .build();
    }
}
