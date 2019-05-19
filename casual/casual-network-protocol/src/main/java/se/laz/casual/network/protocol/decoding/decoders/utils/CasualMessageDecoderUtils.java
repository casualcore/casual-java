/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.utils;

import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;
import se.laz.casual.api.util.Pair;
import se.laz.casual.api.xa.XID;
import se.laz.casual.api.xa.XIDFormatType;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.utils.ByteUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-02.
 */
public final class CasualMessageDecoderUtils
{
    private static final int UUID_NETWORK_SIZE = 16;
    private static final int STRING_NETWORK_SIZE = 8;

    // These are the same for service call request/reply
    private static final int SERVICE_BUFFER_TYPE_SUBNAME_NETWORK_SIZE = 8;
    private static final int SERVICE_BUFFER_PAYLOAD_NETWORK_SIZE = 8;

    private CasualMessageDecoderUtils()
    {}
    public static UUID getAsUUID(final byte[] message)
    {
        final ByteBuffer mostSignificant = ByteBuffer.wrap(message, 0,
            message.length/2);
        final ByteBuffer leastSignificant = ByteBuffer.wrap(message, message.length/2,
            message.length/2);
        return new UUID(mostSignificant.getLong(), leastSignificant.getLong());
    }

    public static String getAsString(final byte[] bytes, int offset, int length, final Charset charset)
    {
        return new String(bytes, offset, length, charset);
    }

    public static String getAsString(final byte[] bytes, int offset, int length)
    {
        return getAsString(bytes, offset, length, StandardCharsets.UTF_8);
    }

    public static String getAsString(final byte[] bytes, final Charset charset)
    {
        return getAsString(bytes, 0, bytes.length, charset);
    }

    public static String getAsString(final byte[] bytes)
    {
        return getAsString(bytes, StandardCharsets.UTF_8);
    }

    public static String readString(final AsynchronousByteChannel channel, int length)
    {
        final ByteBuffer stringBuffer;
        stringBuffer = ByteUtils.readFully(channel, length).join();
        return getAsString(stringBuffer.array());
    }

    public static String readString(final ReadableByteChannel channel, int length)
    {
        final ByteBuffer stringBuffer = ByteUtils.readFully(channel, length);
        return getAsString(stringBuffer.array());
    }

    /**
     * Use this to read a string from the channel when you know that the structure is as follows
     * 8 bytes for the string size
     * the string of string size
     * @param channel
     * @return
     */
    public static String readString(final AsynchronousByteChannel channel)
    {
        final int stringSize = (int)ByteUtils.readFully(channel, STRING_NETWORK_SIZE).join().getLong();
        return readString(channel, stringSize);
    }

    /**
     * Use this to read a string from the channel when you know that the structure is as follows
     * 8 bytes for the string size
     * the string of string size
     * @param channel
     * @return
     */
    public static String readString(final ReadableByteChannel channel)
    {
        final int stringSize = (int)ByteUtils.readFully(channel, STRING_NETWORK_SIZE).getLong();
        return readString(channel, stringSize);
    }

    /**
     * Used to get dynamic array content
     * Use when the whole payload fits in one byte[]
     * @param bytes - complete message
     * @param index - index where to start in the message
     * @param numberOfItemsNetworkSize - The network byte size to read how many items there are in the array
     * @param itemNetworkSize - The network byte size per item in the array
     * @param converter - A converter to convert the data to your type
     * @param <T> - Your type
     * @return - DynamicArrayIndexPair that contains a list of the data converted to your type and the current offset into bytes
     */
    public static <T> DynamicArrayIndexPair<T> getDynamicArrayIndexPair(byte[] bytes, int index, int numberOfItemsNetworkSize, int itemNetworkSize, final ItemConverterWithOffset<T> converter)
    {
        int currentOffset = index;
        final List<T> items = new ArrayList<>();
        final long numberOfItems = ByteBuffer.wrap(bytes, currentOffset, numberOfItemsNetworkSize).getLong();
        currentOffset += numberOfItemsNetworkSize;
        for(int i = 0; i < numberOfItems; ++i)
        {
            final int itemSize = (int) ByteBuffer.wrap(bytes, currentOffset, itemNetworkSize).getLong();
            currentOffset += itemNetworkSize;
            final T item = converter.convertItem(bytes, currentOffset, itemSize);
            currentOffset += itemSize;
            items.add(item);
        }
        return DynamicArrayIndexPair.of(items, currentOffset);
    }

    /**
     * Used when reading payloads that exceed Integer.MAX_VALUE bytes
     * Use to get data from any dynamic arrays in a message
     * Note - the structure of the message list has to be as follows:
     * First byte array contains only the number of things to read
     * Each subsequent arrays are to be in pairs:
     * 1st the byte size of that buffer
     * 2nd is the actual data
     * @ return A DynamicArrayIndexPair that contains the data, converted using the supplied converter, and the current index in the list
     */
    public static <T> DynamicArrayIndexPair<T> getDynamicArrayIndexPair(List<byte[]> message, int index, ItemConverter<T> converter)
    {
        int currentIndex = index;
        List<T> items = new ArrayList<>();
        ByteBuffer numberOfItemsBuffer = ByteBuffer.wrap(message.get(currentIndex++));
        final long numberItems = numberOfItemsBuffer.getLong();
        for(int i = 0; i < numberItems; ++i)
        {
            ByteBuffer itemSizeBuffer = ByteBuffer.wrap(message.get(currentIndex++));
            final int itemSize = (int)itemSizeBuffer.getLong();
            final byte[] nameBytes = message.get(currentIndex++);
            if (nameBytes.length != itemSize)
            {
                throw new CasualProtocolException("itemSize: " + itemSize + " but buffer has a length of " + nameBytes.length);
            }
            final T serviceName = converter.convertItem(nameBytes);
            items.add(serviceName);
        }
        return DynamicArrayIndexPair.of(items, currentIndex);
    }

    public static UUID readUUID(final AsynchronousByteChannel channel)
    {
        final ByteBuffer executionBuffer = ByteUtils.readFully(channel, UUID_NETWORK_SIZE).join();
        return getAsUUID(executionBuffer.array());
    }

    public static UUID readUUID(final ReadableByteChannel channel)
    {
        final ByteBuffer executionBuffer = ByteUtils.readFully(channel, UUID_NETWORK_SIZE);
        return getAsUUID(executionBuffer.array());
    }

    public static ServiceBuffer readServiceBuffer(final ReadableByteChannel channel, int maxPayloadSize)
    {
        final int bufferTypeNameSize = (int) ByteUtils.readFully(channel, SERVICE_BUFFER_TYPE_SUBNAME_NETWORK_SIZE).getLong();
        final String bufferTypename = CasualMessageDecoderUtils.readString(channel, bufferTypeNameSize);
        final long payloadSize = ByteUtils.readFully(channel, SERVICE_BUFFER_PAYLOAD_NETWORK_SIZE).getLong();
        final List<byte[]> payload = readPayload(channel, payloadSize, maxPayloadSize);
        return ServiceBuffer.of(bufferTypename, payload);
    }

    public static Pair<Integer, ServiceBuffer> readServiceBuffer(final byte[] bytes, int offset)
    {
        int currentOffset = offset;
        int serviceBufferTypeSize = (int) ByteBuffer.wrap(bytes, currentOffset, CommonSizes.SERVICE_BUFFER_TYPE_SIZE.getNetworkSize()).getLong();
        currentOffset += CommonSizes.SERVICE_BUFFER_TYPE_SIZE.getNetworkSize();
        String serviceTypeName = CasualMessageDecoderUtils.getAsString(bytes, currentOffset, serviceBufferTypeSize);
        currentOffset += serviceBufferTypeSize;
        // this can be huge, ie not fitting into one ByteBuffer
        // but since the whole message fits into Integer.MAX_VALUE that is not true of this message
        int serviceBufferPayloadSize = (int) ByteBuffer.wrap(bytes, currentOffset, CommonSizes.SERVICE_BUFFER_PAYLOAD_SIZE.getNetworkSize()).getLong();
        currentOffset += CommonSizes.SERVICE_BUFFER_PAYLOAD_SIZE.getNetworkSize();
        byte[] payloadData = Arrays.copyOfRange(bytes, currentOffset, currentOffset + serviceBufferPayloadSize);
        List<byte[]> serviceBufferPayload = new ArrayList<>();
        serviceBufferPayload.add(payloadData);
        ServiceBuffer serviceBuffer = ServiceBuffer.of(serviceTypeName, serviceBufferPayload);
        currentOffset += serviceBufferPayloadSize;
        return Pair.of(currentOffset, serviceBuffer);
    }

    public static Pair<Integer, Xid> readXid(final byte[] data, int offset)
    {
        int currentOffset = offset;
        long xidFormat = ByteBuffer.wrap(data, currentOffset, CommonSizes.XID_FORMAT.getNetworkSize()).getLong();
        currentOffset += CommonSizes.XID_FORMAT.getNetworkSize();
        Xid xid = XID.NULL_XID;
        if(!XIDFormatType.isNullType(xidFormat))
        {
            int gtridLength = (int)ByteBuffer.wrap(data, currentOffset, CommonSizes.XID_GTRID_LENGTH.getNetworkSize()).getLong();
            currentOffset += CommonSizes.XID_GTRID_LENGTH.getNetworkSize();
            int bqualLength = (int)ByteBuffer.wrap(data, currentOffset, CommonSizes.XID_BQUAL_LENGTH.getNetworkSize()).getLong();
            currentOffset += CommonSizes.XID_BQUAL_LENGTH.getNetworkSize();
            ByteBuffer xidPayloadBuffer = ByteBuffer.wrap(data, currentOffset, gtridLength + bqualLength);
            final byte[] xidPayload = new byte[gtridLength + bqualLength];
            xidPayloadBuffer.get(xidPayload);
            currentOffset += (gtridLength + bqualLength);
            xid = XID.of(gtridLength, bqualLength, xidPayload, xidFormat);
        }
        return Pair.of(currentOffset, xid);
    }

    private static List<byte[]> readPayload(final ReadableByteChannel channel, long payloadSize, int maxPayloadSize)
    {
        return payloadSize <= maxPayloadSize ? readPayloadSingleBuffer(channel, (int)payloadSize)
            : readPayloadChunked(channel, payloadSize, maxPayloadSize);
    }


    private static List<byte[]> readPayloadSingleBuffer(final ReadableByteChannel channel, int payloadSize)
    {
        final List<byte[]> l = new ArrayList<>();
        ByteBuffer payload = ByteUtils.readFully(channel, payloadSize);
        l.add(payload.array());
        return l;
    }

    private static List<byte[]> readPayloadChunked(final ReadableByteChannel channel, long payloadSize, int maxSingleBufferByteSize)
    {
        long toRead = payloadSize;
        long read = 0;
        final List<byte[]> l = new ArrayList<>();
        while ((toRead - maxSingleBufferByteSize) > 0)
        {
            final ByteBuffer chunk = ByteUtils.readFully(channel, maxSingleBufferByteSize);
            l.add(chunk.array());
            toRead -= maxSingleBufferByteSize;
            // can we overflow?
            read += maxSingleBufferByteSize;
        }
        int leftToRead = (int) (payloadSize - read);
        if (leftToRead > 0)
        {
            final ByteBuffer chunk = ByteUtils.readFully(channel, leftToRead);
            l.add(chunk.array());
        }
        return l;
    }

}
