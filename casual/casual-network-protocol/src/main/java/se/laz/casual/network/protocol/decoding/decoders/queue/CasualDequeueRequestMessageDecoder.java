/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.queue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.messages.parseinfo.DequeueRequestSizes;
import se.laz.casual.network.protocol.messages.queue.CasualDequeueRequestMessage;
import se.laz.casual.network.protocol.utils.ByteUtils;
import se.laz.casual.network.protocol.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
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
    public CasualDequeueRequestMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        ByteBuf buffer = Unpooled.wrappedBuffer(b.array());
        CasualDequeueRequestMessage msg = getMessage(buffer);
        buffer.release();
        return msg;
    }

    @Override
    public CasualDequeueRequestMessage readChunked(final ReadableByteChannel channel)
    {
        UUID execution = CasualMessageDecoderUtils.readUUID(channel);
        int queueNameSize = (int) ByteUtils.readFully(channel, DequeueRequestSizes.NAME_SIZE.getNetworkSize()).getLong();
        String queueName = CasualMessageDecoderUtils.readString(channel, queueNameSize);
        Xid xid = XIDUtils.readXid(channel);
        int selectorPropertiesSize = (int) ByteUtils.readFully(channel, DequeueRequestSizes.SELECTOR_PROPERTIES_SIZE.getNetworkSize()).getLong();
        String selectorProperties = (0 == selectorPropertiesSize) ? "" : CasualMessageDecoderUtils.readString(channel, selectorPropertiesSize);
        UUID selectorId = CasualMessageDecoderUtils.readUUID(channel);
        boolean block = (1 == ByteUtils.readFully(channel, DequeueRequestSizes.BLOCK.getNetworkSize()).get());
        return CasualDequeueRequestMessage.createBuilder()
                                          .withExecution(execution)
                                          .withQueueName(queueName)
                                          .withXid(xid)
                                          .withSelectorProperties(selectorProperties)
                                          .withSelectorUUID(selectorId)
                                          .withBlock(block)
                                          .build();
    }

    @Override
    public CasualDequeueRequestMessage readSingleBuffer(final ByteBuf buffer)
    {
        return getMessage(buffer);
    }

    private static CasualDequeueRequestMessage getMessage(final ByteBuf buffer)
    {
        UUID execution = CasualMessageDecoderUtils.readUUID(buffer);
        int queueNameSize = (int)buffer.readLong();
        byte[] queueNameBuffer = new byte[queueNameSize];
        buffer.readBytes(queueNameBuffer);
        String queueName = CasualMessageDecoderUtils.getAsString(queueNameBuffer);
        Xid xid = CasualMessageDecoderUtils.readXid(buffer);
        int selectorPropertiesSize = (int)buffer.readLong();
        String selectorProperties = CasualMessageDecoderUtils.readAsString(buffer, selectorPropertiesSize);
        UUID selectorId = CasualMessageDecoderUtils.readUUID(buffer);
        boolean block = (1 == buffer.readByte());
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
