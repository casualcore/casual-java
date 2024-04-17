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

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class FromJSONEventMessageDecoder extends SimpleChannelInboundHandler<Object>
{
    private static final Logger LOG = Logger.getLogger(FromJSONEventMessageDecoder.class.getName());
    private final EventObserver observer;
    private final CompletableFuture<Boolean> eventFuture;

    private FromJSONEventMessageDecoder(EventObserver observer, CompletableFuture<Boolean> eventFuture)
    {
        this.observer = observer;
        this.eventFuture = eventFuture;
    }
    public static FromJSONEventMessageDecoder of(EventObserver observer, CompletableFuture<Boolean> connectedFuture)
    {
        return new FromJSONEventMessageDecoder(observer, connectedFuture);
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg)
    {
        ByteBuf content = (ByteBuf)msg;
        String json = content.toString(CharsetUtil.UTF_8);
        ServiceCallEvent event =  JsonProviderFactory.getJsonProvider().fromJson(json, ServiceCallEvent.class);
        // omnipotent
        eventFuture.complete(true);
        observer.notify(event);
        LOG.finest(() -> "read msg: " + event + " on channel: " + channelHandlerContext.channel());
    }
}
