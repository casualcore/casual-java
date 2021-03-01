/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config

import se.laz.casual.api.external.json.JsonProviderFactory
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class CasualConfigTest extends Specification
{
    def setup()
    {
    }

    @Unroll
    def "Get config from file"()
    {
        given:
        String config = new File( "src/test/resources/" + file ).getText( StandardCharsets.UTF_8.name(  ) )
        Configuration expected = Configuration.newBuilder()
                .withInbound( Inbound.newBuilder()
                        .withStartup( Startup.newBuilder()
                                .withMode( mode )
                                .withServices( services )
                                .build() )
                        .build() )
                .build()

        when:
        Configuration actual = JsonProviderFactory.getJsonProvider(  ).fromJson( new StringReader( config ), Configuration.class )

        then:
        actual == expected

        where:
        file                           || mode           | services
        "casual-config-immediate.json" || Mode.IMMEDIATE | []
        "casual-config-trigger.json"   || Mode.TRIGGER   | [Mode.Constants.TRIGGER_SERVICE]
        "casual-config-discover.json"  || Mode.DISCOVER  | ["service1", "service2"]
    }

}
