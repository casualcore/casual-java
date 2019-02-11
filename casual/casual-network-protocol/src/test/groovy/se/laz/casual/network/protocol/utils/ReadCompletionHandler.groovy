/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.utils

import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousByteChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.CompletableFuture

/**
 * Created by aleph on 2017-03-16.
 */
class ReadCompletionHandler implements CompletionHandler<Integer, Object>
{
    final ByteBuffer buffer
    final CompletableFuture<ByteBuffer> future
    final AsynchronousByteChannel channel
    private ReadCompletionHandler(ByteBuffer buffer, CompletableFuture<ByteBuffer> future, AsynchronousByteChannel channel)
    {
        this.buffer = buffer
        this.future = future
        this.channel = channel
    }

    static ReadCompletionHandler of(ByteBuffer buffer, CompletableFuture<ByteBuffer> future, AsynchronousByteChannel channel)
    {
        return new ReadCompletionHandler(buffer, future, channel)
    }

    @Override
    void completed(Integer result, Object attachment)
    {
        if(result < 0)
        {
            // connection closed before finishing reading
            future.completeExceptionally(new CasualProtocolException("connection closed!"))
        }
        else if(buffer.remaining() > 0)
        {
            channel.read(buffer, null, this)
        }
        else
        {
            // prepare for reading
            buffer.flip()
            future.complete(buffer)
        }
    }

    @Override
    void failed(Throwable e, Object attachment)
    {
        future.completeExceptionally(new CasualProtocolException("reading from network failed", e))
    }
}
