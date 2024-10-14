/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config

import se.laz.casual.config.json.Mode
import spock.lang.Specification

class ConfigurationStoreTest extends Specification
{
    ConfigurationStore instance

    def setup()
    {
        instance = new ConfigurationStore()
    }

    def "Retrieve using a string key"()
    {
        given:
        String expected = "latin1"
        instance.put( ConfigurationOptions.CASUAL_API_FIELDED_ENCODING, expected )

        when:
        String actual = instance.get( ConfigurationOptions.CASUAL_API_FIELDED_ENCODING )

        then:
        actual == expected
    }

    def "Retrieve using an enum key"()
    {
        given:
        Mode expected = Mode.TRIGGER
        instance.put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE, expected )

        when:
        Mode actual = instance.get( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE )

        then:
        actual == expected
    }

    def "Retrieve using a List<String> key"()
    {
        given:
        List<String> expected = ["service1","service2"]
        instance.put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_SERVICES, expected )

        when:
        List<String> actual = instance.get( ConfigurationOptions.CASUAL_INBOUND_STARTUP_SERVICES )

        then:
        actual == expected
    }
}
