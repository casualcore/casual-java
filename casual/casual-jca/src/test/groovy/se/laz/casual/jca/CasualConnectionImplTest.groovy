/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.flags.AtmiFlags
import se.laz.casual.api.flags.Flag
import se.laz.casual.api.queue.MessageSelector
import se.laz.casual.api.queue.QueueInfo
import se.laz.casual.api.queue.QueueMessage
import se.laz.casual.jca.queue.CasualQueueCaller
import se.laz.casual.jca.service.CasualServiceCaller
import se.laz.casual.network.connection.CasualConnectionException
import spock.lang.Shared
import spock.lang.Specification

class CasualConnectionImplTest extends Specification
{
    @Shared CasualConnectionImpl instance
    @Shared CasualManagedConnection connection

    def setup()
    {
        connection = Mock( )
        instance = new CasualConnectionImpl(connection)

    }

    def "Connection is valid when it has a managed connection reference."()
    {
        expect:
        instance.isInvalid() == false
    }

    def "Connection is invalid after being invalidated."()
    {
        when:
        instance.invalidate()
        then:
        instance.isInvalid() == true
    }

    def "Close informs managed connection that it is being closed."()
    {
        when:
        instance.close()
        then:
        1 * connection.closeHandle( instance )
    }

    def "Close when connection is invalidated throws exception."()
    {
        when:
        instance.invalidate()
        instance.close()
        then:
        thrown CasualConnectionException
    }

    def "GetManagedConnection returns the current managed connection"()
    {
        expect:
        instance.getManagedConnection() == connection
    }

    def "GetManagedConnection after invalidation returns null"()
    {
        when:
        instance.invalidate()
        then:
        instance.getManagedConnection() == null
    }

    def "SetManagedConnection updates the reference managed connection"()
    {
        setup:
        CasualManagedConnection newConnection = Mock(CasualManagedConnection )
        when:
        instance.setManagedConnection( newConnection )
        then:
        instance.getManagedConnection() == newConnection
    }

    def "Tpcall when invalidated throws exception."()
    {
        when:
        instance.invalidate()
        instance.tpcall( "test", JsonBuffer.of( "{}"), Flag.of( AtmiFlags.NOFLAG))
        then:
        thrown CasualConnectionException
    }

    def "Tpacall when invalidated throws exception."()
    {
        when:
        instance.invalidate()
        instance.tpacall( "test", JsonBuffer.of( "{}"), Flag.of( AtmiFlags.NOFLAG))
        then:
        thrown CasualConnectionException
    }

    def 'enqueue when invalidated throws exception'()
    {
        when:
        instance.invalidate()
        instance.enqueue(QueueInfo.createBuilder()
                                  .withQspace('qspace')
                                  .withQname('qname')
                                  .build(), QueueMessage.of(JsonBuffer.of( "{}")))
        then:
        thrown CasualConnectionException
    }

    def 'dequeue when invalidated throws exception'()
    {
        when:
        instance.invalidate()
        instance.dequeue(QueueInfo.createBuilder()
                                  .withQspace('qspace')
                                  .withQname('qname')
                                  .build(), MessageSelector.of())
        then:
        thrown CasualConnectionException
    }

    def "Tpcall forwards request to CasaulServiceCaller."()
    {
        setup:
        CasualServiceCaller serviceCallerMock = Mock(CasualServiceCaller)
        instance.setCasualServiceCaller( serviceCallerMock )
        when:
        instance.tpcall( "test", JsonBuffer.of( "{}"), Flag.of( AtmiFlags.NOFLAG ))
        then:
        1 * serviceCallerMock.tpcall( "test", JsonBuffer.of( "{}"), Flag.of( AtmiFlags.NOFLAG ))
    }

    def "Tpacall forwards request to CasaulServiceCaller."()
    {
        setup:
        CasualServiceCaller serviceCallerMock = Mock(CasualServiceCaller)
        instance.setCasualServiceCaller( serviceCallerMock )
        when:
        instance.tpacall( "test", JsonBuffer.of( "{}"), Flag.of( AtmiFlags.NOFLAG ))
        then:
        1 * serviceCallerMock.tpacall( "test", JsonBuffer.of( "{}"), Flag.of( AtmiFlags.NOFLAG ))
    }

    def "enqueue forwards request to CasaulServiceCaller"()
    {
        setup:
        CasualQueueCaller queueCaller = Mock(CasualQueueCaller)
        ((CasualConnectionImpl) instance).queueCaller = queueCaller
        def msg = QueueMessage.of(JsonBuffer.of( "{}"))
        when:
        instance.enqueue(QueueInfo.createBuilder()
                .withQspace('qspace')
                .withQname('qname')
                .build(), msg)
        then:
        1 * queueCaller.enqueue(QueueInfo.createBuilder()
                .withQspace('qspace')
                .withQname('qname')
                .build(), msg)
    }

    def "dequeue forwards request to CasaulServiceCaller"()
    {
        setup:
        CasualQueueCaller queueCaller = Mock(CasualQueueCaller)
        ((CasualConnectionImpl) instance).queueCaller = queueCaller
        when:
        instance.dequeue(QueueInfo.createBuilder()
                .withQspace('qspace')
                .withQname('qname')
                .build(), MessageSelector.of())
        then:
        1 * queueCaller.dequeue(QueueInfo.createBuilder()
                .withQspace('qspace')
                .withQname('qname')
                .build(), MessageSelector.of())
    }

    def "getCasaulServiceCaller when not test injected returns a new instance"()
    {
        expect:
        instance.getCasualServiceCaller() != null
    }

    def "Equals and HashCode tests confirm only equal through referential equality."()
    {
        setup:
        CasualConnectionImpl instance2 = new CasualConnectionImpl( connection )

        expect:
        instance.equals( instance )
        ! instance.equals( null )
        ! instance.equals( instance2 )

        instance.hashCode() == instance.hashCode()
        instance.hashCode() != instance2.hashCode()
    }

    def "toString test"()
    {
        expect:
        instance.toString().contains( "CasualConnectionImpl")
    }
}
