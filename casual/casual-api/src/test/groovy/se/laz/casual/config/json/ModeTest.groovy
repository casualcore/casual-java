/*
 * Copyright (c) 2021 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json

import se.laz.casual.api.external.json.JsonProviderFactory
import spock.lang.Specification

class ModeTest extends Specification
{
    def "Json roundtrip check."()
    {
        given:
        String json = JsonProviderFactory.getJsonProvider(  ).toJson( instance )

        when:
        Mode actual = JsonProviderFactory.getJsonProvider(  ).fromJson( json, Mode.class )

        then:
        actual == instance

        where:
        instance << [Mode.IMMEDIATE, Mode.TRIGGER, Mode.DISCOVER ]
    }
}
