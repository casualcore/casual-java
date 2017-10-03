package se.kodarkatten.casual.jca

import se.kodarkatten.casual.jca.CasualConnectionFactory
import se.kodarkatten.casual.jca.CasualConnectionFactoryImpl
import se.kodarkatten.casual.jca.CasualManagedConnectionFactory
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.naming.Reference
import javax.resource.spi.ConnectionManager

class CasualConnectionFactoryImplTest extends Specification
{
    @Shared CasualConnectionFactory instance
    @Shared CasualManagedConnectionFactory factory, factory2
    @Shared ConnectionManager cm, cm2
    @Shared Reference r, r2

    def setup()
    {
        factory = Mock( CasualManagedConnectionFactory )
        cm = Mock( ConnectionManager )
        r = Mock( Reference )

        factory2 = Mock( CasualManagedConnectionFactory )
        cm2 = Mock( ConnectionManager )
        r2 = Mock( Reference )

        instance = new CasualConnectionFactoryImpl( factory, cm )
    }

    def cleanup()
    {
        instance = null
    }

    def "GetConnection"()
    {
        when:
        instance.getConnection()

        then:
        1 * cm.allocateConnection( factory, null )
    }

    def "GetReference before it is set then it is null."()
    {
        expect:
        instance.getReference() == null
    }

    def "GetReference once set return the set reference."()
    {
        when:
        instance.setReference( r )
        then:
        instance.getReference() == r
    }

    @Unroll
    def "Equals and Hashcode permutations."()
    {
        setup:
        instance = new CasualConnectionFactoryImpl( mcf1, m1 )
        instance.setReference( ref1 )
        CasualConnectionFactoryImpl instance2 = new CasualConnectionFactoryImpl( mcf2, m2 )
        instance2.setReference( ref2 )

        expect:
        instance.equals( instance2 ) == expectedResult
        (instance.hashCode( ) == instance2.hashCode() ) == expectedResult

        where:
        mcf1    | mcf2      | m1    | m2    | ref1  | ref2  || expectedResult
        factory | factory   | cm    | cm    | r     | r     || true
        factory | null      | cm    | cm    | r     | r     || false
        null    | factory   | cm    | cm    | r     | r     || false
        null    | null      | cm    | cm    | r     | r     || true
        factory | factory2  | cm    | cm    | r     | r     || false
        factory2| factory   | cm    | cm    | r     | r     || false
        factory | factory   | cm    | null  | r     | r     || false
        factory | factory   | cm    | null  | r     | r     || false
        factory | factory   | null  | cm    | r     | r     || false
        factory | factory   | null  | null  | r     | r     || true
        factory | factory   | cm    | cm2   | r     | r     || false
        factory | factory   | cm2   | cm    | r     | r     || false
        factory | factory   | cm    | cm    | r     | null  || false
        factory | factory   | cm    | cm    | null  | r     || false
        factory | factory   | cm    | cm    | null  | null  || true
        factory | factory   | cm    | cm    | r     | r2    || false
        factory | factory   | cm    | cm    | r2    | r     || false
        factory | factory2  | cm    | cm2   | r     | r2    || false
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
        instance.toString().contains( "CasualConnectionFactoryImpl" )
    }
}
