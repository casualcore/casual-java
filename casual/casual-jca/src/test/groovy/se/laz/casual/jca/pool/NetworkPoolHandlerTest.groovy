/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.pool

import se.laz.casual.jca.Address
import se.laz.casual.network.connection.CasualConnectionException
import se.laz.casual.network.outbound.NetworkListener
import spock.lang.Specification

class NetworkPoolHandlerTest extends Specification
{
    def 'setup'()
    {
        // note: @ for field not getter
        NetworkPoolHandler.getInstance().@pools.clear()
    }

    def 'Get empty pools'()
    {
        when:
        Map<String, NetworkConnectionPool> actual = NetworkPoolHandler.getInstance(  ).getPools(  )

        then:
        actual == [:]
    }

    def 'Get empty pool by name is null'()
    {
        when:
        NetworkConnectionPool actual = NetworkPoolHandler.getInstance(  ).getPool( "invalid" )

        then:
        actual == null
    }

    def 'reverse and no connection available'()
    {
        given:
        def poolName = 'reverse-handler-pool'
        NetworkConnectionPool pool = NetworkPoolHandler.getInstance().getOrCreateReversePool( poolName )

        when: 'allocation fails since no EIS has connected yet'
        NetworkPoolHandler.getInstance().getOrCreate( poolName, Address.of( 'asdf', 123 ), Mock( NetworkListener ), 1 )

        then: 'it throws but the reverse pool remains registered, it is filled as EIS(s) connects'
        thrown( CasualConnectionException )
        NetworkPoolHandler.getInstance().getPool( poolName ) == pool
        pool.isReverse()
        pool.getPoolDomainIds().isEmpty()
    }
}
