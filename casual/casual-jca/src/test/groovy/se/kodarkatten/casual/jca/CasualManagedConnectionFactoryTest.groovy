package se.kodarkatten.casual.jca

import se.kodarkatten.casual.jca.CasualConnectionFactoryImpl
import se.kodarkatten.casual.jca.CasualManagedConnection
import se.kodarkatten.casual.jca.CasualManagedConnectionFactory
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.resource.ResourceException
import javax.resource.spi.ConnectionManager
import javax.resource.spi.ConnectionRequestInfo
import javax.resource.spi.ManagedConnection
import javax.resource.spi.ResourceAdapter
import javax.security.auth.Subject

class CasualManagedConnectionFactoryTest extends Specification
{
    @Shared CasualManagedConnectionFactory instance
    @Shared ResourceAdapter ra1 = Mock(ResourceAdapter)
    @Shared ResourceAdapter ra2 = Mock(ResourceAdapter)

    def setup()
    {
        instance = new CasualManagedConnectionFactory( )
    }

    def "GetHostName returns null if not set."()
    {
        expect:
        instance.getHostName() == null
    }

    def "SetHostName saves host name and can be retrieved."()
    {
        setup:
        String hostname = "expected.sfa.se"

        when:
        instance.setHostName( hostname )

        then:
        instance.getHostName() == hostname
    }

    def "GetPortNumber returns null if not set."()
    {
        expect:
        instance.getPortNumber() == null
    }

    def "SetPortNumber saves port number and can be retrieved."()
    {
        setup:
        Integer port = 1234

        when:
        instance.setPortNumber( port )

        then:
        instance.getPortNumber() == port
    }

    def "CreateConnectionFactory without a connection manager is not allowed."()
    {
        when:
        instance.createConnectionFactory()

        then:
        thrown ResourceException
    }

    def "CreateConnectionFactory with a connection manager returns a CasualConnectionFactory instance."()
    {
        setup:
        ConnectionManager manager = Mock(ConnectionManager)

        when:
        Object factory = instance.createConnectionFactory( manager )

        then:
        factory instanceof CasualConnectionFactoryImpl
    }

    def "CreateManagedConnection returns a new instance of a CasualManagedConnection."()
    {
        setup:
        Subject s = new Subject()
        ConnectionRequestInfo cri = Mock(ConnectionRequestInfo)

        when:
        ManagedConnection m = instance.createManagedConnection( s, cri )

        then:
        m instanceof CasualManagedConnection
    }

    def "MatchManagedConnections returns a CasualManagedConnection from the set provided."()
    {
        setup:
        Subject subject = new Subject()
        ConnectionRequestInfo cri = Mock(ConnectionRequestInfo)
        ManagedConnection connection = new CasualManagedConnection( null, null );
        Set<Object> set = new HashSet<>()
        set.add( connection )

        expect:
        instance.matchManagedConnections( set, subject, cri ) == connection
    }

    def "MatchManagedConnections returns null if there are no CasualManagedConnection instance in the set provided."()
    {
        setup:
        Subject subject = new Subject()
        ConnectionRequestInfo cri = Mock(ConnectionRequestInfo)
        ManagedConnection connection = Mock(ManagedConnection)
        Set<Object> set = new HashSet<>()
        set.add( connection )

        expect:
        instance.matchManagedConnections( set, subject, cri ) == null

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

    def "GetResourceAdapter is initially null."()
    {
        expect:
        instance.getResourceAdapter() == null
    }

    def "SetResourceAdapter saves the resource adapter which can be retrieved with get."()
    {
        setup:
        ResourceAdapter ra = Mock(ResourceAdapter)

        when:
        instance.setResourceAdapter( ra )

        then:
        instance.getResourceAdapter() == ra
    }

    @Unroll
    def "Equals / Hashcode #res1, #res2, #host1, #host2, #port1, #port2 == #result"( )
    {
        expect:
        instance.setResourceAdapter( res1 )
        instance.setHostName( host1 )
        instance.setPortNumber( port1 )

        CasualManagedConnectionFactory instance2 = new CasualManagedConnectionFactory()
        instance2.setResourceAdapter( res2 )
        instance2.setHostName( host2 )
        instance2.setPortNumber( port2 )

        instance.equals( instance2 ) == result
        (instance.hashCode() == instance2.hashCode() ) == result

        where:
        res1    | res2  | host1             | host2             | port1 | port2 | result
        ra1     | ra1   | "example.sfa.se"  | "example.sfa.se"  | 123   | 123   | true
        ra1     | ra2   | "example.sfa.se"  | "example.sfa.se"  | 123   | 123   | false
        ra2     | ra1   | "example.sfa.se"  | "example.sfa.se"  | 123   | 123   | false
        ra1     | null  | "example.sfa.se"  | "example.sfa.se"  | 123   | 123   | false
        null    | ra1   | "example.sfa.se"  | "example.sfa.se"  | 123   | 123   | false
        null    | null  | "example.sfa.se"  | "example.sfa.se"  | 123   | 123   | true
        ra1     | ra1   | "example.sfa.se"  | "example.sfa.s"   | 123   | 123   | false
        ra1     | ra1   | "example.sfa.s"   | "example.sfa.se"  | 123   | 123   | false
        ra1     | ra1   | "example.sfa.se"  | null              | 123   | 123   | false
        ra1     | ra1   | null              | "example.sfa.se"  | 123   | 123   | false
        ra1     | ra1   | null              | null              | 123   | 123   | true
        ra1     | ra1   | "example.sfa.se"  | "example.sfa.se"  | 123   | 321   | false
        ra1     | ra1   | "example.sfa.se"  | "example.sfa.se"  | 321   | 123   | false
        ra1     | ra1   | "example.sfa.se"  | "example.sfa.se"  | 123   | null  | false
        ra1     | ra1   | "example.sfa.se"  | "example.sfa.se"  | null  | 123   | false
        ra1     | ra1   | "example.sfa.se"  | "example.sfa.se"  | null  | null  | true
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
        instance.toString().contains( "CasualManagedConnectionFactory" )
    }
}
