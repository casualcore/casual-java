package se.kodarkatten.casual.jca

import groovy.json.internal.Charsets
import se.kodarkatten.casual.api.xa.XID
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.transaction.xa.Xid

class CasualTransactionResourcesTest extends Specification
{
    @Shared CasualTransactionResources instance
    @Shared Xid xid1, xid2, xid3

    def setup()
    {
        instance = CasualTransactionResources.getInstance()
        xid1 = XID.of( "123".getBytes(Charsets.UTF_8), "321".getBytes(Charsets.UTF_8), 0 )
        xid2 = XID.of("456".getBytes(Charsets.UTF_8), "654".getBytes(Charsets.UTF_8), 0 )
        xid3 = XID.of( xid1 )
    }

    def cleanup()
    {
        instance.removeResourceIdForXid( xid1 )
        instance.removeResourceIdForXid( xid2 )
        instance.removeResourceIdForXid( xid3 )
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
        CasualTransactionResources second = CasualTransactionResources.getInstance()

        then:
        instance.is( second )
    }

    @Unroll
    def "GetResourceIdForXid returns the correct ids."()
    {
        expect:
        ( instance.getResourceIdForXid( a ) == instance.getResourceIdForXid( b ) ) == c

        where:
        a       | b     || c
        xid1    | xid1  || true
        xid2    | xid2  || true
        xid3    | xid3  || true
        xid1    | xid2  || false
        xid1    | xid3  || true
        xid2    | xid3  || false
    }

    def "XidPending persists in the queue."()
    {
        setup:
        instance.getResourceIdForXid( xid1 )

        expect:
        instance.xidPending( xid1 )
        !instance.xidPending( xid2 )
        instance.xidPending( xid3 )
    }

    def "RemoveResourceIdForXid"()
    {
        setup:
        instance.getResourceIdForXid( xid1 )
        instance.getResourceIdForXid( xid2 )

        when:
        instance.removeResourceIdForXid( xid1 )

        then:
        !instance.xidPending( xid1 )
        instance.xidPending( xid2 )
        !instance.xidPending( xid3 )
    }

    def "toString test."()
    {
        expect:
        instance.toString().contains( "CasualTransactionResources" )
    }
}
