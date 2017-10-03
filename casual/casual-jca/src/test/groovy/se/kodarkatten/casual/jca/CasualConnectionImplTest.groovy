package se.kodarkatten.casual.jca

import se.kodarkatten.casual.api.buffer.CasualBuffer
import se.kodarkatten.casual.api.buffer.type.JsonBuffer
import se.kodarkatten.casual.api.flags.AtmiFlags
import se.kodarkatten.casual.api.flags.Flag
import se.kodarkatten.casual.jca.CasualConnectionImpl
import se.kodarkatten.casual.jca.CasualManagedConnection
import se.kodarkatten.casual.jca.service.CasualServiceCaller
import se.kodarkatten.casual.network.connection.CasualConnectionException
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
        instance.tpcall( "test", JsonBuffer.of( "{}"), Flag.of( AtmiFlags.NOFLAG), CasualBuffer.class )
        then:
        thrown CasualConnectionException
    }

    def "Tpacall when invalidated throws exception."()
    {
        when:
        instance.invalidate()
        instance.tpacall( "test", JsonBuffer.of( "{}"), Flag.of( AtmiFlags.NOFLAG), CasualBuffer.class )
        then:
        thrown CasualConnectionException
    }

    def "Tpcall forwards request to CasaulServiceCaller."()
    {
        setup:
        CasualServiceCaller serviceCallerMock = Mock(CasualServiceCaller)
        instance.setCasualServiceCaller( serviceCallerMock )
        when:
        instance.tpcall( "test", JsonBuffer.of( "{}"), Flag.of( AtmiFlags.NOFLAG ), CasualBuffer.class )
        then:
        1 * serviceCallerMock.tpcall( "test", JsonBuffer.of( "{}"), Flag.of( AtmiFlags.NOFLAG ), CasualBuffer.class )
    }

    def "Tpacall forwards request to CasaulServiceCaller."()
    {
        setup:
        CasualServiceCaller serviceCallerMock = Mock(CasualServiceCaller)
        instance.setCasualServiceCaller( serviceCallerMock )
        when:
        instance.tpacall( "test", JsonBuffer.of( "{}"), Flag.of( AtmiFlags.NOFLAG ), CasualBuffer.class )
        then:
        1 * serviceCallerMock.tpacall( "test", JsonBuffer.of( "{}"), Flag.of( AtmiFlags.NOFLAG ), CasualBuffer.class )
    }

    def "getCasaulServiceCaller when not test injected returns a new instance"()
    {
        expect:
        instance.getCasualServiceCaller() != null
    }

    def "Equals and HashCode tests confirm only equal through referencial equality."()
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
