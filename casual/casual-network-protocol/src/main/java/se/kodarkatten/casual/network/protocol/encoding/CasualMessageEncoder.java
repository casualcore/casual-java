package se.kodarkatten.casual.network.protocol.encoding;


import io.netty.buffer.ByteBuf;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.network.protocol.messages.exceptions.CasualProtocolException;
import se.kodarkatten.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by aleph on 2017-03-12.
 */
public final class CasualMessageEncoder
{
    private CasualMessageEncoder()
    {}

    public static <T extends CasualNetworkTransmittable> void write(final WritableByteChannel channel, final CasualNWMessage<T> msg)
    {
        for(final byte[] bytes : msg.toNetworkBytes())
        {
            ByteUtils.writeFully(channel, ByteBuffer.wrap(bytes), bytes.length);
        }
    }

    public static <T extends CasualNetworkTransmittable> void write(final ByteBuf out, final CasualNWMessage<T> msg)
    {
        for(byte[] b : msg.toNetworkBytes())
        {
            out.writeBytes(b);
        }
    }
}
