/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import se.laz.casual.api.xa.XID
import se.laz.casual.internal.network.NetworkConnection
import spock.lang.Shared
import spock.lang.Specification

import javax.resource.NotSupportedException
import javax.resource.ResourceException
import javax.resource.spi.ConnectionEvent
import javax.resource.spi.ConnectionEventListener
import javax.transaction.xa.XAResource
import javax.transaction.xa.Xid

class CasualManagedConnectionTest extends Specification
{

    @Shared CasualManagedConnection instance
    @Shared CasualManagedConnectionFactory managedConnectionFactory

    def setup()
    {
        managedConnectionFactory = Mock(CasualManagedConnectionFactory)

        instance = new CasualManagedConnection( managedConnectionFactory )

        NetworkConnection networkConnection = Mock( )
        networkConnection.isActive() >> true
        instance.networkConnection = networkConnection
    }

    def "GetNetworkConnection returns same instance."()
    {
        when:
        NetworkConnection first = instance.getNetworkConnection()
        NetworkConnection second = instance.getNetworkConnection()

        then:
        first != null
        second != null
        first == second

    }

    def "GetConnection"()
    {
        when:
        Object connection = instance.getConnection( null, null)

        then:
        connection instanceof CasualConnectionImpl
        ((CasualConnectionImpl)connection).getManagedConnection() == instance
    }

    def "GetConnection ping failure"()
    {
        setup:
        NetworkConnection networkConnection = Mock( )
        networkConnection.isActive() >> false
        instance.networkConnection = networkConnection
        when:
        instance.getConnection( null, null)
        then:
        thrown(ResourceException)
    }



    def "AssociateConnection with null throws NullPointerException"()
    {
        when:
        instance.associateConnection( null )

        then:
        thrown NullPointerException
    }

    def "AssociateConnection with the wrong class of Connection"()
    {
        when:
        instance.associateConnection( "a string which clearly isn't a connection." )

        then:
        thrown ResourceException
    }

    def "AssociateConnection with a managed connection factory instance and the connection now has a reference to it."()
    {
        setup:
        CasualManagedConnection initial = new CasualManagedConnection( managedConnectionFactory )
        instance != initial
        CasualConnectionImpl connection = new CasualConnectionImpl( initial )
        connection.getManagedConnection() == initial

        when:
        instance.associateConnection( connection )

        then:
        connection.getManagedConnection() == instance

    }

    def "Cleanup test"()
    {
        CasualConnectionImpl connection = instance.getConnection( null, null )
        connection.isInvalid() == false

        when:
        instance.cleanup()

        then:
        connection.isInvalid() == true
    }

    def "Destroy"()
    {
        setup:
        NetworkConnection networkConnection = Mock( )
        instance.networkConnection = networkConnection

        when:
        instance.destroy()

        then:
        1 * networkConnection.close()

    }

    def "GetLogWriter is initially null."()
    {
        expect:
        instance.getLogWriter() == null
    }

    def "SetLogWriter saves the print write which can be the retrieved with get."()
    {
        setup:
        File f = File.createTempFile( "printwriter", "txt" )
        PrintWriter p = new PrintWriter( f )

        when:
        instance.setLogWriter( p )

        then:
        instance.getLogWriter() == p
    }

    def "GetLocalTransaction throws NotSupportedException as this is not supported."()
    {
        when:
        instance.getLocalTransaction()

        then:
        thrown NotSupportedException
    }

    def "GetXAResource"()
    {
        when:
        XAResource resource = instance.getXAResource()
        XAResource resource2 = instance.getXAResource()

        then:
        resource != null
        resource2 != null
        resource == resource2
    }

    def "GetCurrentXid when no XAResource returns null xid."()
    {
        expect:
        instance.getCurrentXid() == XID.NULL_XID
    }

    def "GetCurrentXid XAResource exists and returns XAResource XID."()
    {
        setup:
        Xid xid = XID.NULL_XID
        CasualXAResource resource = instance.getXAResource()
        resource.start( xid, 0 )

        expect:
        instance.getCurrentXid() == xid
    }

    def "GetMetaData returns an object."()
    {
        expect:
        instance.getMetaData() != null
    }

    def "CloseHandle sends a connection close event."()
    {
        setup:
        CasualConnection connection = instance.getConnection( null, null )
        ConnectionEventListener listener = Mock(ConnectionEventListener)
        instance.addConnectionEventListener( listener )
        ConnectionEvent event

        when:
        instance.closeHandle( connection )

        then:
        1 * listener.connectionClosed( _ ) >> { args -> event = args[0] }
        verifyConnectionEvent( event )
    }

    def verifyConnectionEvent( ConnectionEvent event )
    {
        event.getSource() == instance
        event.getId() == ConnectionEvent.CONNECTION_CLOSED
    }

    def "CloseHandle sends a connection close event to all registered ConnectionEventListeners."()
    {
        setup:
        CasualConnection connection = instance.getConnection(null, null)
        ConnectionEventListener listener = Mock(ConnectionEventListener)
        ConnectionEventListener listener2 = Mock(ConnectionEventListener)
        instance.addConnectionEventListener(listener)
        instance.addConnectionEventListener(listener2)
        ConnectionEvent event1, event2

        when:
        instance.closeHandle(connection)

        then:
        1 * listener.connectionClosed(_) >> { args -> event1 = args[0] }
        verifyConnectionEvent( event1 )

        1 * listener2.connectionClosed(_) >> { args -> event2 = args[0] }
        verifyConnectionEvent( event2 )
    }

    def "CloseHandle sends a connection close event to only still registered ConnectionEventListeners."()
    {
        setup:
        CasualConnection connection = instance.getConnection( null, null )
        ConnectionEventListener listener = Mock(ConnectionEventListener)
        ConnectionEventListener listener2 = Mock(ConnectionEventListener)
        instance.addConnectionEventListener( listener )
        instance.addConnectionEventListener( listener2 )
        ConnectionEvent event

        when:
        instance.removeConnectionEventListener( listener2 )
        instance.closeHandle( connection )

        then:
        1 * listener.connectionClosed( _ ) >> { args -> event = args[0] }
        verifyConnectionEvent( event )

        0 * listener2.connectionClosed( _ )
    }

    def "Equals and HashCode tests confirm only equal through referential equality."()
    {
        setup:
        CasualManagedConnection instance2 = new CasualManagedConnection( managedConnectionFactory )

        expect:
        instance.equals( instance )
        ! instance.equals( null )
        ! instance.equals( instance2 )

        instance.hashCode() == instance.hashCode()
        instance.hashCode() != instance2.hashCode()
    }

    def "toString test."()
    {
        expect:
        instance.toString().contains( "CasualManagedConnection" )
    }
}
