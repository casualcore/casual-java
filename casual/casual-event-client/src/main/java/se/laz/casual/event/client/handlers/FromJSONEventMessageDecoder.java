/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.client.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import se.laz.casual.api.external.json.JsonProviderFactory;
import se.laz.casual.event.ServiceCallEvent;
import se.laz.casual.event.client.EventObserver;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class FromJSONEventMessageDecoder extends SimpleChannelInboundHandler<Object>
{
    private static final Logger LOG = Logger.getLogger(FromJSONEventMessageDecoder.class.getName());
    private final CompletableFuture<Boolean> connectFuture;

    private enum State{
        READ_CONNECT_REPLY, READ_EVENT
    }
    private State state = State.READ_CONNECT_REPLY;
    private final EventObserver observer;

    private FromJSONEventMessageDecoder(EventObserver observer, CompletableFuture<Boolean> connectFuture)
    {
        Objects.requireNonNull(observer, "observer must not be null");
        Objects.requireNonNull(connectFuture, "connectFuture must not be null");
        this.observer = observer;
        this.connectFuture = connectFuture;
    }
    public static FromJSONEventMessageDecoder of(EventObserver observer, CompletableFuture<Boolean> connectFuture)
    {
        return new FromJSONEventMessageDecoder(observer, connectFuture);
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg)
    {
        ByteBuf content = (ByteBuf)msg;
        String json = content.toString(CharsetUtil.UTF_8);
        if(state == State.READ_CONNECT_REPLY)
        {
            connectFuture.complete(true);
            state = State.READ_EVENT;
        }
        else
        {
            ServiceCallEvent event = JsonProviderFactory.getJsonProvider().fromJson(json, ServiceCallEvent.class);
            observer.notify(event);
            LOG.finest(() -> "read msg: " + event + " on channel: " + channelHandlerContext.channel());
        }
    }
}
