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
class WriteCompletionHandler implements CompletionHandler<Integer, Object>
{
    CompletableFuture<Void> future = new CompletableFuture<>()
    ByteBuffer buffer
    AsynchronousByteChannel channel
    private WriteCompletionHandler(CompletableFuture<Void> future, ByteBuffer buffer, AsynchronousByteChannel channel)
    {
        this.future = future
        this.buffer = buffer
        this.channel = channel
    }

    static WriteCompletionHandler of(CompletableFuture<Void> future, ByteBuffer buffer, AsynchronousByteChannel channel)
    {
        return new WriteCompletionHandler(future, buffer, channel)
    }

    @Override
    void completed(Integer result, Object attachment)
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
    void failed(Throwable e, Object attachment)
    {
        future.completeExceptionally(new CasualProtocolException("writing to network failed", e));
    }
}
