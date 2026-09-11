/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import se.laz.casual.jca.pool.NetworkPoolHandler
import se.laz.casual.network.outbound.NettyNetworkConnection
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.naming.Reference
import jakarta.resource.spi.ConnectionManager

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
    def 'normal pool queries do not allocate a connection nor create a pool'()
    {
        given:
        String poolName = "pool-${UUID.randomUUID()}"
        DomainId domainId = DomainId.of(UUID.randomUUID())
        factory.getNetworkConnectionPoolName() >> poolName
        factory.getNetworkConnectionPoolSize() >> 1

        when:
        boolean reverse = instance.isReverse()
        def domains = instance.getDomainIds()
        boolean disconnecting = instance.isDomainDisconnecting()
        boolean domainDisconnecting = instance.isDomainDisconnecting(domainId)

        then:
        !reverse
        domains.isEmpty()
        !disconnecting
        !domainDisconnecting
        NetworkPoolHandler.getInstance().getPool(poolName) == null
        0 * cm.allocateConnection(_, _)
    }

    def 'registered empty reverse pool domain disconnected queries without allocating any connection'()
    {
        given:
        String poolName = "pool-${UUID.randomUUID()}"
        // what CasualResourceAdapter does for a configured reverse pool on startup
        def handler = NetworkPoolHandler.getInstance()
        handler.getOrCreateReversePool(poolName)
        factory.getNetworkConnectionPoolName() >> poolName
        factory.getNetworkConnectionPoolSize() >> 1

        when:
        boolean reverse = instance.isReverse()
        def domains = instance.getDomainIds()
        boolean disconnecting = instance.isDomainDisconnecting(DomainId.of(UUID.randomUUID()))

        then:
        reverse
        domains.isEmpty()
        !disconnecting
        0 * cm.allocateConnection(_, _)

        when:
        instance.isDomainDisconnecting()

        then:
        thrown(IllegalStateException)
        0 * cm.allocateConnection(_, _)

        cleanup:
        handler.@pools.remove(poolName)
    }

    def 'factories query their own pools even when the domain id is shared'()
    {
        given:
        String firstPoolName = "pool-${UUID.randomUUID()}"
        String secondPoolName = "pool-${UUID.randomUUID()}"
        DomainId domainId = DomainId.of(UUID.randomUUID())
        // what CasualResourceAdapter does for configured reverse pools on startup
        def handler = NetworkPoolHandler.getInstance()
        def firstPool = handler.getOrCreateReversePool(firstPoolName)
        def secondPool = handler.getOrCreateReversePool(secondPoolName)
        firstPool.addConnectionForReversePool(Mock(NettyNetworkConnection) {
            getDomainId() >> domainId
            isDomainDisconnecting() >> true
        })
        secondPool.addConnectionForReversePool(Mock(NettyNetworkConnection) {
            getDomainId() >> domainId
            isDomainDisconnecting() >> false
        })
        factory.getNetworkConnectionPoolName() >> firstPoolName
        factory.getNetworkConnectionPoolSize() >> 1
        factory2.getNetworkConnectionPoolName() >> secondPoolName
        factory2.getNetworkConnectionPoolSize() >> 1
        def secondFactory = new CasualConnectionFactoryImpl(factory2, cm2)

        when:
        boolean firstDisconnecting = instance.isDomainDisconnecting(domainId)
        boolean secondDisconnecting = secondFactory.isDomainDisconnecting(domainId)
        def firstDomains = instance.getDomainIds()
        def secondDomains = secondFactory.getDomainIds()

        then:
        firstDisconnecting
        !secondDisconnecting
        firstDomains == [domainId]
        secondDomains == [domainId]
        0 * cm.allocateConnection(_, _)
        0 * cm2.allocateConnection(_, _)

        cleanup:
        handler.@pools.remove(firstPoolName)
        handler.@pools.remove(secondPoolName)
    }

}
