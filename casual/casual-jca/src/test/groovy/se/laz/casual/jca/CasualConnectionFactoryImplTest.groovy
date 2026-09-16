/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import se.laz.casual.jca.pool.NetworkPoolHandler
import se.laz.casual.jca.pool.NetworkConnectionPool
import jakarta.resource.spi.ResourceAllocationException
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

    def 'disconnecting normal pool rejects acquisition before allocation'()
    {
        given:
        String poolName = "pool-${UUID.randomUUID()}"
        def handler = NetworkPoolHandler.getInstance()
        def pool = Mock(NetworkConnectionPool)
        handler.@pools.put(poolName, pool)
        factory.getNetworkConnectionPoolName() >> poolName
        factory.getNetworkConnectionPoolSize() >> 1

        when:
        instance.getConnection()

        then:
        1 * pool.isDomainDisconnecting() >> true
        thrown(ResourceAllocationException)
        0 * cm.allocateConnection(_, _)

        cleanup:
        handler.@pools.remove(poolName)
    }

    def "GetConnection"()
    {
        given:
        factory.getNetworkConnectionPoolName() >> "pool-${UUID.randomUUID()}"
        factory.getNetworkConnectionPoolSize() >> 1

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

    def 'explicit request info is preserved when allocation succeeds'()
    {
        given:
        def info = Mock(jakarta.resource.spi.ConnectionRequestInfo)
        def connection = Mock(CasualConnection)
        factory.getNetworkConnectionPoolName() >> "pool-${UUID.randomUUID()}"
        factory.getNetworkConnectionPoolSize() >> 1

        when:
        def result = instance.getConnection(info)

        then:
        1 * cm.allocateConnection(factory, info) >> connection
        result.is(connection)
    }

    def 'disconnecting pool rejects explicit request info before allocation'()
    {
        given:
        String poolName = "pool-${UUID.randomUUID()}"
        def handler = NetworkPoolHandler.getInstance()
        handler.@pools.put(poolName, Mock(NetworkConnectionPool) {
            isDomainDisconnecting() >> true
        })
        factory.getNetworkConnectionPoolName() >> poolName
        factory.getNetworkConnectionPoolSize() >> 1

        when:
        instance.getConnection(Mock(jakarta.resource.spi.ConnectionRequestInfo))

        then:
        thrown(ResourceAllocationException)
        0 * cm.allocateConnection(_, _)

        cleanup:
        handler.@pools.remove(poolName)
    }

    def 'missing pool query does not create a pool or allocate a connection'()
    {
        given:
        String poolName = "pool-${UUID.randomUUID()}"
        factory.getNetworkConnectionPoolName() >> poolName
        factory.getNetworkConnectionPoolSize() >> 1

        when:
        def disconnecting = instance.isDomainDisconnecting()

        then:
        !disconnecting
        NetworkPoolHandler.getInstance().getPool(poolName) == null
        0 * cm.allocateConnection(_, _)
    }

}
