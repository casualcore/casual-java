/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.pool

import spock.lang.Specification

class NetworkPoolHandlerTest extends Specification
{

    def "Get empty pools"()
    {
        when:
        Map<String, NetworkConnectionPool> actual = NetworkPoolHandler.getInstance(  ).getPools(  )

        then:
        actual == [:]
    }

    def "Get empty pool by name is null."()
    {
        when:
        NetworkConnectionPool actual = NetworkPoolHandler.getInstance(  ).getPool( "invalid" )

        then:
        actual == null
    }
}
