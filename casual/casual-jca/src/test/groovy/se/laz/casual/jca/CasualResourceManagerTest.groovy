/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import se.laz.casual.api.xa.XID
import spock.lang.Shared
import spock.lang.Specification

import javax.transaction.xa.Xid
import java.nio.charset.StandardCharsets

class CasualResourceManagerTest extends Specification
{
    @Shared CasualResourceManager instance
    @Shared Xid xid1, xid2, xid3
    @Shared Address domainOne, domainTwo

    def setup()
    {
        instance = CasualResourceManager.getInstance()
        xid1 = XID.of( "123".getBytes(StandardCharsets.UTF_8), "321".getBytes(StandardCharsets.UTF_8), 0 )
        xid2 = XID.of("456".getBytes(StandardCharsets.UTF_8), "654".getBytes(StandardCharsets.UTF_8), 0 )
        xid3 = XID.of( xid1 )
        domainOne= Address.of('foo', 2134)
        domainTwo = Address.of('bar', 4132)
    }

    def cleanup()
    {
        instance.remove(domainOne, xid1 )
        instance.remove(domainOne, xid2 )
        instance.remove(domainOne, xid3 )
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

    def "XidPending xid1."()
    {
        setup:
        instance.put( domainOne, xid1 )

        expect:
        instance.isPending( domainOne, xid1 )
        !instance.isPending( domainOne, xid2 )
        instance.isPending( domainOne, xid3 )
    }

    def "RemoveResourceIdForXid"()
    {
        setup:
        instance.put( domainOne, xid1 )
        instance.put( domainOne, xid2 )

        when:
        instance.remove( domainOne, xid1 )

        then:
        !instance.isPending( domainOne, xid1 )
        instance.isPending( domainOne, xid2 )
        !instance.isPending( domainOne, xid3 )
    }

    def 'add same xid twice'()
    {
        given:
        instance.put( domainOne, xid1 )
        when:
        instance.put( domainOne, xid1 )
        then:
        def e = thrown(CasualResourceAdapterException)
        e.message == "xid: ${xid1} already stored for domain: ${domainOne}"
    }

    def 'two resources, two domains'()
    {
       when:
       instance.put(domainOne, xid1)
       instance.put(domainTwo, xid1)
       then:
       instance.isPending(domainOne, xid1)
       instance.isPending(domainTwo, xid1)
       when:
       instance.remove(domainOne, xid1)
       then:
       !instance.isPending(domainOne, xid1)
       instance.isPending(domainTwo, xid1)
    }

    def "toString test."()
    {
        expect:
        instance.toString().contains( "CasualResourceManager" )
    }
}
