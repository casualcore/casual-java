package se.kodarkatten.casual.network.io.readers;

import java.nio.channels.AsynchronousByteChannel;

/**
 * Created by aleph on 2017-03-28.
 */
public interface NetworkReader<T>
{
    T readSingleBuffer(final AsynchronousByteChannel channel, int messageSize);
    T readChunked(final AsynchronousByteChannel channel);
}
