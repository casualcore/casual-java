package se.kodarkatten.casual.jca

import groovy.json.internal.Charsets
import se.kodarkatten.casual.api.xa.XID
import spock.lang.Shared
import spock.lang.Specification

import javax.transaction.xa.Xid

class CasualResourceManagerTest extends Specification
{
    @Shared CasualResourceManager instance
    @Shared Xid xid1, xid2, xid3

    def setup()
    {
        instance = CasualResourceManager.getInstance()
        xid1 = XID.of( "123".getBytes(Charsets.UTF_8), "321".getBytes(Charsets.UTF_8), 0 )
        xid2 = XID.of("456".getBytes(Charsets.UTF_8), "654".getBytes(Charsets.UTF_8), 0 )
        xid3 = XID.of( xid1 )
    }

    def cleanup()
    {
        instance.remove( xid1 )
        instance.remove( xid2 )
        instance.remove( xid3 )
    }

    def "Check xid constraints"()
    {
        expect:
        xid1 != xid2
        !xid1.is( xid3 )
        xid1 == xid3

        xid1.hashCode().equals( xid1.hashCode() )
        xid1.hashCode().equals( xid3.hashCode() )
    }

    def "GetInstance returns the same instance"()
    {
        when:
        CasualResourceManager second = CasualResourceManager.getInstance()

        then:
        instance.is( second )
    }

    def "XidPending persists in the queue."()
    {
        setup:
        instance.put( xid1 )

        expect:
        instance.isPending( xid1 )
        !instance.isPending( xid2 )
        instance.isPending( xid3 )
    }

    def "RemoveResourceIdForXid"()
    {
        setup:
        instance.put( xid1 )
        instance.put( xid2 )

        when:
        instance.remove( xid1 )

        then:
        !instance.isPending( xid1 )
        instance.isPending( xid2 )
        !instance.isPending( xid3 )
    }

    def 'add same xid twice'()
    {
        given:
        instance.put( xid1 )
        when:
        instance.put( xid1 )
        then:
        def e = thrown(CasualResourceAdapterException)
        e.message == "xid: ${xid1} already stored"
    }

    def "toString test."()
    {
        expect:
        instance.toString().contains( "CasualResourceManager" )
    }
}
