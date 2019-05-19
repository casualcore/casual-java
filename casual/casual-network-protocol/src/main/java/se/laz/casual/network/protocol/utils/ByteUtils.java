/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.utils;

import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
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
                .reduce(0L,(sum, v) -> sum += v);
    }

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
                    future.completeExceptionally(new CasualProtocolException("connection closed!"));
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
                future.completeExceptionally(new CasualProtocolException("reading from network failed", e));
            }
        });
        return future;
    }

    public static ByteBuffer readFully(final ReadableByteChannel channel, int length)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(length);
        int numberOfBytesRead = 0;
        int totalNumberOfBytesRead = 0;
        while(-1 != numberOfBytesRead && buffer.remaining() > 0)
        {
            try
            {
                // not using += is not an error, we only look if eos has been reached
                // for knowing if we've read fully we check the buffer.remaining()
                numberOfBytesRead = channel.read(buffer);
                totalNumberOfBytesRead += numberOfBytesRead;
            }
            catch (IOException e)
            {
                throw new CasualProtocolException("failed reading fully, number of bytes read: " + numberOfBytesRead + "\n" + e);
            }
        }
        totalNumberOfBytesRead += (-1 == numberOfBytesRead) ? 1 : 0;
        if(totalNumberOfBytesRead != length)
        {
            throw new CasualProtocolException("expected to read: " + length + " but could only read: " + totalNumberOfBytesRead + " bytes, broken pipe?");
        }
        //prepare for reading
        buffer.flip();
        return buffer;
    }

    public static CompletableFuture<Void> writeFully(final AsynchronousByteChannel channel, final ByteBuffer buffer)
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
                    future.completeExceptionally(new CasualProtocolException("connection closed!"));
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
                future.completeExceptionally(new CasualProtocolException("writing to network failed", e));
            }
        });
        return future;
    }

    public static void writeFully(final WritableByteChannel channel, final ByteBuffer buffer, int byteLen)
    {
        int bytesWritten = 0;
        while(bytesWritten < byteLen)
        {
            try
            {
                bytesWritten += channel.write(buffer);
            }
            catch (IOException e)
            {
                throw new CasualProtocolException("failed writing fully, bytes written: " + bytesWritten + " out of " + byteLen + " bytes", e);
            }
        }
    }


}
