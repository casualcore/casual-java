/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.info

import se.laz.casual.network.messages.domain.TransactionType
import spock.lang.Specification

class CasualInfoTest extends Specification
{
    static final Service service = new Service.Builder()
            .name( "test" )
            .jndiName( "jndi" )
            .transactionType( TransactionType.ATOMIC )
            .timeout( 10 )
            .hops( 4 )
            .category("C"  )
    .build(  );

    def "add inbound service"()
    {
        when:
        CasualInfo.addInboundService( service )

        then:
        CasualInfo.getInboundService(service.getName(  )  ).isPresent(  )
        CasualInfo.getInboundService(service.getName(  )  ).get(  ) == service
    }
}
