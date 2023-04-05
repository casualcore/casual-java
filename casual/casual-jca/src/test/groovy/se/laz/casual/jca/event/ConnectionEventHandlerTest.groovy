/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.event

import se.laz.casual.network.connection.CasualConnectionException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import jakarta.resource.spi.ConnectionEvent
import jakarta.resource.spi.ConnectionEventListener
import jakarta.resource.spi.ManagedConnection

class ConnectionEventHandlerTest extends Specification
{
    @Shared ConnectionEventHandler instance
    @Shared ConnectionEventListener listener, listener2
    @Shared ManagedConnection managedConnection
    @Shared ConnectionEvent eventInstance

    def setup()
    {
        listener = Mock( ConnectionEventListener )
        listener2 = Mock( ConnectionEventListener )
        managedConnection = Mock( ManagedConnection )
        instance = new ConnectionEventHandler()
    }

    def "AddConnectionEventListener saves listeners as shown by the number of listeners."()
    {
        when:
        instance.addConnectionEventListener( listener )

        then:
        instance.listenerCount() == 1
    }

    def "AddConnectionEventListener with a null listener throws NullPointerException."()
    {
        when:
        instance.addConnectionEventListener( null )

        then:
        thrown NullPointerException
        instance.listenerCount() == 0
    }

    def "RemoveConnectionEventListener"()
    {
        setup:
        instance.addConnectionEventListener( listener )

        when:
        instance.removeConnectionEventListener( listener )

        then:
        instance.listenerCount() == 0
    }

    def "RemoveConnectionEventListener with a null listener throws NullPointerException."()
    {
        setup:
        instance.addConnectionEventListener( listener )

        when:
        instance.removeConnectionEventListener( null )

        then:
        thrown NullPointerException
        instance.listenerCount() == 1
    }

    @Unroll
    def "SendEvent #event should call listener method #methodName"()
    {
        setup:
        instance.addConnectionEventListener( listener )
        ConnectionEvent actualEvent = null

        when:
        eventInstance = new ConnectionEvent(managedConnection, event )
        instance.sendEvent( eventInstance )

        then:
        1 * listener./$methodName/(_) >> { ConnectionEvent e -> actualEvent = e }
        actualEvent.getId() == event

        where:
        event                                       || methodName
        ConnectionEvent.CONNECTION_CLOSED           || "connectionClosed"
        ConnectionEvent.CONNECTION_ERROR_OCCURRED   || "connectionErrorOccurred"
        ConnectionEvent.LOCAL_TRANSACTION_COMMITTED || "localTransactionCommitted"
        ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK|| "localTransactionRolledback"
        ConnectionEvent.LOCAL_TRANSACTION_STARTED   || "localTransactionStarted"
    }

    def "SendEvent unknown event throws CasualException"()
    {
        setup:
        instance.addConnectionEventListener( listener )

        when:
        eventInstance = new ConnectionEvent(managedConnection, 123123 )
        instance.sendEvent( eventInstance )

        then:
        thrown CasualConnectionException
    }

    def "Equals and HashCode permutations."()
    {
        setup:
        ConnectionEventHandler instance2 = new ConnectionEventHandler()
        if( list1 != null )
        {
            instance.addConnectionEventListener( list1 )
        }
        if( list2 != null )
        {
            instance2.addConnectionEventListener( list2 )
        }

        expect:
        instance.equals( instance2 ) == expectedResult
        (instance.hashCode() ==  instance2.hashCode() ) == expectedResult

        where:
        list1       | list2     || expectedResult
        listener    | listener  || true
        listener    | null      || false
        null        | listener  || false
        null        | null      || true
        listener    | listener2 || false
        listener2   | listener  || false
    }

    def "Equals object checks."()
    {
        expect:
        instance.equals( instance )
        ! instance.equals( null )
        ! instance.equals( "" )
    }

    def "toString test."()
    {
        expect:
        instance.toString().contains( "ConnectionEventHandler" )
    }
}
