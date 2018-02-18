package se.kodarkatten.casual.network.protocol.io.readers;

import se.kodarkatten.casual.api.network.protocol.messages.CasualNetworkTransmittable;

import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;

/**
 * Created by aleph on 2017-03-28.
 */
public final class MessageReader<T extends CasualNetworkTransmittable>
{
    final NetworkReader<T> networkReader;
    final int maxSingleBufferByteSize;
    // It is very much used thank you
    @SuppressWarnings("squid:UnusedPrivateMethod")
    private MessageReader(final NetworkReader<T> networkReader, int maxSingleBufferByteSize)
    {
        this.networkReader = networkReader;
        this.maxSingleBufferByteSize = maxSingleBufferByteSize;
    }

    public static <T extends CasualNetworkTransmittable> MessageReader<T> of(final NetworkReader<T> r)
    {
        return of(r, Integer.MAX_VALUE);
    }

    public static <T extends CasualNetworkTransmittable> MessageReader<T> of(final NetworkReader<T> r, int maxSingleBufferByteSize)
    {
        Objects.requireNonNull(r, "networkReader can not be null!");
        return new MessageReader<>(r, maxSingleBufferByteSize);
    }

    /**
     * It is upon the caller to close the channel
     * @param channel
     * @param messageSize
     * @return
     */
    // caller should close the channel, we have no idea what they want to do
    @SuppressWarnings("squid:S2095")
    public T read(final AsynchronousByteChannel channel, long messageSize)
    {
        Objects.requireNonNull(channel, "channel is null");
        if (messageSize <= maxSingleBufferByteSize)
        {
            return networkReader.readSingleBuffer(channel, (int) messageSize);
        }
        return networkReader.readChunked(channel);
    }
    @SuppressWarnings("squid:S2095")
    public T read(final ReadableByteChannel channel, long messageSize)
    {
        Objects.requireNonNull(channel, "channel is null");
        if (messageSize <= maxSingleBufferByteSize)
        {
            return networkReader.readSingleBuffer(channel, (int) messageSize);
        }
        return networkReader.readChunked(channel);
    }

    public T read(final byte[] data)
    {
        Objects.requireNonNull(data, "data is null");
        return networkReader.readSingleBuffer(data);
    }

}
