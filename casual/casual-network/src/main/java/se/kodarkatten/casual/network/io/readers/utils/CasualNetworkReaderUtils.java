package se.kodarkatten.casual.network.io.readers.utils;

import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by aleph on 2017-03-02.
 */
public final class CasualNetworkReaderUtils
{
    private CasualNetworkReaderUtils()
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

    public static String readString(final AsynchronousByteChannel channel, int length) throws ExecutionException, InterruptedException
    {
        final ByteBuffer stringBuffer = ByteUtils.readFully(channel, length).get();
        return getAsString(stringBuffer.array());
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
    public static <T> DynamicArrayIndexPair getDynamicArrayIndexPair(byte[] bytes, int index, int numberOfItemsNetworkSize, int itemNetworkSize, final ItemConverterWithOffset<T> converter)
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
    public static <T> DynamicArrayIndexPair getDynamicArrayIndexPair(List<byte[]> message, int index, ItemConverter<T> converter)
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
                throw new CasualTransportException("itemSize: " + itemSize + " but buffer has a length of " + nameBytes.length);
            }
            final T serviceName = converter.convertItem(nameBytes);
            items.add(serviceName);
        }
        return DynamicArrayIndexPair.of(items, currentIndex);
    }

}
