package se.kodarkatten.casual.network.utils;

import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by aleph on 2017-03-03.
 */
public final class ByteUtils
{
    private ByteUtils()
    {}

    // We suppress this since it is bogus
    @SuppressWarnings("squid:AssignmentInSubExpressionCheck")
    public static long sumNumberOfBytes(List<byte[]> l)
    {
        return l.stream()
                .map(b -> (long)b.length)
                .reduce(0l,(sum, v) -> sum += v);
    }

    // No this warning makes no sense here - this is very much readable code
    @SuppressWarnings("squid:S1188")
    public static CompletableFuture<ByteBuffer> readFully(AsynchronousByteChannel channel, int length)
    {
        CompletableFuture<ByteBuffer> future = new CompletableFuture<>();
        final ByteBuffer buffer = ByteBuffer.allocate(length);
        channel.read(buffer, null, new CompletionHandler<Integer, Object>()
        {
            @Override
            public void completed(Integer result, Object attachment)
            {
                if(result < 0)
                {
                    // connection closed before finishing reading
                    future.completeExceptionally(new CasualTransportException("connection closed!"));
                }
                else if(buffer.remaining() > 0)
                {
                    channel.read(buffer, null, this);
                }
                else
                {
                    // prepare for reading
                    buffer.flip();
                    future.complete(buffer);
                }
            }

            @Override
            public void failed(Throwable e, Object attachment)
            {
                future.completeExceptionally(new CasualTransportException("reading from network failed", e));
            }
        });
        return future;
    }

    // No this warning makes no sense here - this is very much readable code
    @SuppressWarnings("squid:S1188")
    public static CompletableFuture<Void> writeFully(AsynchronousByteChannel channel, final ByteBuffer buffer)
    {
        CompletableFuture<Void> future = new CompletableFuture<>();
        channel.write(buffer, null, new CompletionHandler<Integer, Object>()
        {
            @Override
            public void completed(Integer result, Object attachment)
            {
                if(result < 0)
                {
                    // connection closed before finishing writing
                    future.completeExceptionally(new CasualTransportException("connection closed!"));
                }
                else if(buffer.remaining() > 0)
                {
                    channel.write(buffer, null, this);
                }
                else
                {
                    future.complete(null);
                }
            }

            @Override
            public void failed(Throwable e, Object attachment)
            {
                future.completeExceptionally(new CasualTransportException("writing to network failed", e));
            }
        });
        return future;
    }

}
