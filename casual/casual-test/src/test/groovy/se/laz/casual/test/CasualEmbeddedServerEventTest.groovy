/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test

import se.laz.casual.api.flags.ErrorState
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import se.laz.casual.event.client.EventClient
import se.laz.casual.event.client.EventClientBuilder
import spock.lang.Shared
import spock.lang.Specification

import javax.transaction.xa.Xid
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CasualEmbeddedServerEventTest extends Specification
{
    @Shared CasualEmbeddedServer instance

    EventClient client

    String service1 = "test1"
    String parent1 = "parent"
    int pid1 = 123
    UUID execution1 = UUID.randomUUID()
    Xid transactionId1 = Mock(Xid)
    long pending1 = 5L
    ErrorState code1 = ErrorState.OK
    Order order1 = Order.CONCURRENT

    Instant start1 = ZonedDateTime.parse( "2024-04-15T12:34:56.123456Z", DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant()
    Instant end1 = ZonedDateTime.parse( "2024-04-15T12:35:04.123456Z", DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant()
    ServiceCallEvent event = ServiceCallEvent.createBuilder(  )
            .withService(service1)
            .withParent(parent1)
            .withPID(pid1)
            .withExecution(execution1)
            .withTransactionId(transactionId1)
            .withPending( pending1 )
            .withStart( start1 )
            .withEnd( end1 )
            .withCode(code1)
            .withOrder(order1)
            .build()
    CountDownLatch countDownLatch = new CountDownLatch( 1 )


    def setupSpec()
    {
        instance = CasualEmbeddedServer.newBuilder()
                .eventServerEnabled( true )
                .build()
        instance.start()
    }

    def cleanupSpec()
    {
        if ( instance != null && instance.isRunning() )
        {
            instance.shutdown()
        }
    }

    def setup()
    {
        client = EventClientBuilder.createBuilder().withHost( "127.0.0.1").withPort( instance.getEventServerPort().get() )
                .withEventObserver( { it -> System.out.println( "Received event: "+ it ); countDownLatch.countDown() } )
                .withConnectionObserver( { System.out.println( "Connection closed." )} )
                .build()
    }

    def cleanup()
    {
        if( client != null )
        {
            client.close()
        }
    }

    def "connect to event server, receive event, disconnect."()
    {
        when:
        client.connect().get()
        instance.publishEvent( event )

        countDownLatch.await( 6, TimeUnit.SECONDS )

        then:
        countDownLatch.getCount() == 0
    }

    def "Create 2 parallel embedded server instance."()
    {
        given:
        CasualEmbeddedServer instance2 = CasualEmbeddedServer.newBuilder( ).eventServerEnabled( true ).build()
        instance2.start()
        CasualEmbeddedServer instance3 = CasualEmbeddedServer.newBuilder( ).eventServerEnabled( true ).build()
        instance3.start()

        CountDownLatch latch2 = new CountDownLatch( 2 )
        EventClient client2 = EventClientBuilder.createBuilder().withHost( "127.0.0.1").withPort( instance2.getEventServerPort().get() )
                .withEventObserver( { it -> System.out.println( "Received event 2: "+ it ); latch2.countDown() } )
                .withConnectionObserver( { System.out.println( "Connection closed 2." )} )
                .build()

        EventClient client3 = EventClientBuilder.createBuilder().withHost( "127.0.0.1").withPort( instance3.getEventServerPort().get() )
                .withEventObserver( { it -> System.out.println( "Received event 3: "+ it ); latch2.countDown() } )
                .withConnectionObserver( { System.out.println( "Connection closed 3." )} )
                .build()

        CompletableFuture future2 = client2.connect()
        CompletableFuture future3 = client3.connect()
        future2.get()
        future3.get()

        when:
        instance2.publishEvent( event )
        instance3.publishEvent( event )

        latch2.await( 1, TimeUnit.SECONDS )

        then:
        latch2.getCount() == 0

        cleanup:
        client2.close()
        client3.close()
        instance3.shutdown()
        instance2.shutdown()
    }

    def "Create 1 server instance, check disconnect on client occurs."()
    {
        given:
        CasualEmbeddedServer instance2 = CasualEmbeddedServer.newBuilder( ).eventServerEnabled( true ).build()
        instance2.start()

        CountDownLatch latch2 = new CountDownLatch( 1 )
        CountDownLatch disLatch2 = new CountDownLatch( 1 )
        EventClient client2 = EventClientBuilder.createBuilder().withHost( "127.0.0.1").withPort( instance2.getEventServerPort().get() )
                .withEventObserver( { it -> System.out.println( "Received event 2: "+ it ); latch2.countDown() } )
                .withConnectionObserver( { System.out.println( "Connection closed 2." ); disLatch2.countDown(  ) } )
                .build()

        CompletableFuture future2 = client2.connect()
        future2.get()

        when:
        instance2.publishEvent( event )
        instance2.shutdown(  )

        latch2.await( 1, TimeUnit.SECONDS )

        then:
        latch2.getCount() == 0

        when:
        disLatch2.await( 1, TimeUnit.SECONDS)

        then:
        disLatch2.getCount(  ) == 0

        cleanup:
        client2.close()
        instance2.shutdown()
    }


    def "Single server, multiple clients, all receive all published events."()
    {
        given:
        CountDownLatch latch1 = new CountDownLatch( 2 )
        CountDownLatch latch2 = new CountDownLatch( 2 )

        EventClient client2 = EventClientBuilder.createBuilder().withHost( "127.0.0.1").withPort( instance.getEventServerPort().get() )
                .withEventObserver( { it -> System.out.println( "Received event 1: "+ it ); latch1.countDown() } )
                .withConnectionObserver( { System.out.println( "Connection closed 1." )} )
                .build()

        EventClient client3 = EventClientBuilder.createBuilder().withHost( "127.0.0.1").withPort( instance.getEventServerPort().get() )
                .withEventObserver( { it -> System.out.println( "Received event 2: "+ it ); latch2.countDown() } )
                .withConnectionObserver( { System.out.println( "Connection closed 2." )} )
                .build()

        client2.connect().get()
        client3.connect().get()

        when:
        instance.publishEvent( event )
        instance.publishEvent( event )

        latch1.await( 1, TimeUnit.SECONDS )
        latch2.await( 1, TimeUnit.SECONDS )

        then:
        latch1.getCount() == 0
        latch2.getCount() == 0
    }

    def "Publish event null, throws NullPointerException."()
    {
        when:
        instance.publishEvent( null)

        then:
        thrown NullPointerException
    }
}
