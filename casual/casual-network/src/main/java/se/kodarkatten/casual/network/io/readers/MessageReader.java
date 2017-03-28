package se.kodarkatten.casual.network.io.readers;

import java.nio.channels.AsynchronousByteChannel;
import java.util.Objects;

/**
 * Created by aleph on 2017-03-28.
 */
public final class MessageReader<T>
{
    final Readable<T> readable;
    final int maxSingleBufferByteSize;
    private MessageReader(Readable<T> readable, int maxSingleBufferByteSize)
    {
        this.readable = readable;
        this.maxSingleBufferByteSize = maxSingleBufferByteSize;
    }

    public static <T> MessageReader of(final Readable<T> r)
    {
        return of(r, Integer.MAX_VALUE);
    }

    public static <T> MessageReader of(final Readable<T> r, int maxSingleBufferByteSize)
    {
        Objects.requireNonNull(r, "readable can not be null!");
        return new MessageReader(r, maxSingleBufferByteSize);
    }

    public T read(final AsynchronousByteChannel channel, long messageSize)
    {
        Objects.requireNonNull(channel, "channel is null");
        if (messageSize <= maxSingleBufferByteSize)
        {
            return readable.readSingleBuffer(channel, (int) messageSize);
        }
        return readable.readChunked(channel);
    }

}
