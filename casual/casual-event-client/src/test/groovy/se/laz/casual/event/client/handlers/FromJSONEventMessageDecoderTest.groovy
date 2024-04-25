/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.client.handlers

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.util.CharsetUtil
import se.laz.casual.api.external.json.JsonProviderFactory
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.xa.XID
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import se.laz.casual.event.client.EventObserver
import spock.lang.Shared
import spock.lang.Specification

import javax.transaction.xa.Xid
import java.time.Instant
import java.util.concurrent.CompletableFuture

class FromJSONEventMessageDecoderTest extends Specification
{
    @Shared
    Xid transactionId = XID.NULL_XID
    def 'decode'()
    {
        given:
        CompletableFuture<Boolean> connectFuture = new CompletableFuture<>()
        ServiceCallEvent event = createEvent()
        EventObserver observer = Mock(EventObserver){
            1 * notify(event)
        }
        ChannelHandlerContext context = Mock(ChannelHandlerContext)
        ByteBuf connectReplyBuffer = Mock(ByteBuf){
            1 * toString(CharsetUtil.UTF_8) >> {
                '{}'
            }
        }
        ByteBuf eventBuffer = Mock(ByteBuf){
            1 * toString(CharsetUtil.UTF_8) >> {
                JsonProviderFactory.getJsonProvider().toJson(event)
            }
        }
        FromJSONEventMessageDecoder decoder = FromJSONEventMessageDecoder.of(observer, connectFuture)
        when:
        decoder.channelRead0(context, connectReplyBuffer)
        decoder.channelRead0(context, eventBuffer)
        then:
        connectFuture.isDone()
    }

    ServiceCallEvent createEvent()
    {
        return ServiceCallEvent.createBuilder()
                .withCode(ErrorState.OK)
                .withStart(Instant.now())
                .withEnd(Instant.now())
                .withExecution(UUID.randomUUID())
                .withOrder(Order.CONCURRENT)
                .withService("fast-service")
                .withPID(42)
                .withTransactionId(transactionId)
                .build()
    }
}
