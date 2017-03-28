package se.kodarkatten.casual.network.io.readers;

import java.nio.channels.AsynchronousByteChannel;
import java.util.Objects;

/**
 * Created by aleph on 2017-03-28.
 */
public final class MessageReader<T>
{
    final NetworkReader<T> networkReader;
    final int maxSingleBufferByteSize;
    private MessageReader(NetworkReader<T> networkReader, int maxSingleBufferByteSize)
    {
        this.networkReader = networkReader;
        this.maxSingleBufferByteSize = maxSingleBufferByteSize;
    }

    public static <T> MessageReader of(final NetworkReader<T> r)
    {
        return of(r, Integer.MAX_VALUE);
    }

    public static <T> MessageReader of(final NetworkReader<T> r, int maxSingleBufferByteSize)
    {
        Objects.requireNonNull(r, "networkReader can not be null!");
        return new MessageReader(r, maxSingleBufferByteSize);
    }

    public T read(final AsynchronousByteChannel channel, long messageSize)
    {
        Objects.requireNonNull(channel, "channel is null");
        if (messageSize <= maxSingleBufferByteSize)
        {
            return networkReader.readSingleBuffer(channel, (int) messageSize);
        }
        return networkReader.readChunked(channel);
    }

}
