/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.client

import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.handler.codec.json.JsonObjectDecoder
import io.netty.util.CharsetUtil
import se.laz.casual.api.external.json.JsonProviderFactory
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.xa.XID
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import se.laz.casual.event.client.handlers.ConnectionMessageEncoder
import se.laz.casual.event.client.handlers.ExceptionHandler
import se.laz.casual.event.client.handlers.FromJSONEventMessageDecoder
import spock.lang.Shared
import spock.lang.Specification

import javax.transaction.xa.Xid
import java.time.Instant

class EventClientTest extends Specification
{
    @Shared
    EventClientInformation eventClientInformation = EventClientInformation.createBuilder()
            .withConnectionInformation(new ConnectionInformation("localhost", 12345))
            .withEventLoopGroup(new EpollEventLoopGroup())
            .withChannelClass(EpollSocketChannel.class)
            .build()
    @Shared
    EventObserver nopEventObserver = {}
    @Shared
    ConnectionObserver nopConnectionObserver = {}
    @Shared
    EmbeddedChannel channel
    @Shared
    EventClient instance
    @Shared
    Xid transactionId = XID.NULL_XID
    EventObserver eventObserverUnderTest

    def setup()
    {
        EventObserver eventObserver = Mock(EventObserver){
            notify(_) >> { ServiceCallEvent event ->
                getEventObserverUnderTest().notify(event)
            }
        }
        channel = new EmbeddedChannel(ConnectionMessageEncoder.of(), new JsonObjectDecoder(EventClient.MAX_MESSAGE_BYTE_SIZE), FromJSONEventMessageDecoder.of(eventObserver), ExceptionHandler.of())
        instance = new EventClient(channel)
    }

    def 'failed construction'()
    {
        when:
        EventClient.of(connectionInformation, eventObserver, connectionObserver, enableLogging)
        then:
        thrown(NullPointerException)
        where:
        connectionInformation     || eventObserver       || connectionObserver       ||  enableLogging
        null                      || nopEventObserver    || nopConnectionObserver    ||  true
        eventClientInformation    || null                || nopConnectionObserver    ||  true
        eventClientInformation    || nopEventObserver    || null                     ||  true
    }

    def 'event round trip'()
    {
        given:
        ServiceCallEvent event = createEvent()
        eventObserverUnderTest = Mock(EventObserver) {
            1 * notify(event)
        }
        def eventJson = JsonProviderFactory.getJsonProvider().toJson(event)
        byte[] jsonData = eventJson.getBytes(CharsetUtil.UTF_8)
        when:
        instance.connect()
        then:
        channel.outboundMessages().size() == 1
        when:
        channel.writeOneInbound(Unpooled.wrappedBuffer(jsonData))
        then:
        noExceptionThrown()
    }

    def 'closing'()
    {
        given:
        def channel = Mock(Channel){
            1 * close()
        }
        def instance = new EventClient(channel)
        when:
        instance.close()
        then:
        noExceptionThrown()
    }

    EventObserver getEventObserverUnderTest() {
        return null == eventObserverUnderTest ? Mock(EventObserver) : eventObserverUnderTest
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
