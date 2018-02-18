package se.kodarkatten.casual.network.protocol.io.readers;

import se.kodarkatten.casual.api.network.protocol.messages.CasualNetworkTransmittable;

import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by aleph on 2017-03-28.
 */
public interface NetworkReader<T extends CasualNetworkTransmittable>
{
    T readSingleBuffer(final AsynchronousByteChannel channel, int messageSize);
    T readChunked(final AsynchronousByteChannel channel);

    T readSingleBuffer(final ReadableByteChannel channel, int messageSize);
    T readChunked(final ReadableByteChannel channel);

    T readSingleBuffer(final byte[] data);
}
