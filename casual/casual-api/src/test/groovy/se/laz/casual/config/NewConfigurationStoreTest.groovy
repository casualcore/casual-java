/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config

import spock.lang.Specification

class NewConfigurationStoreTest extends Specification
{
    def "Retrieve using a string key"()
    {
        given:
        String expected = "UTF8"

        when:
        String actual = NewConfigurationStore.get( ConfigurationKeys.CASUAL_API_FIELDED_ENCODING )

        then:
        actual == expected

    }

    def "Retrieve using an enum key"()
    {
        given:
        Mode expected = Mode.TRIGGER

        when:
        Mode actual = NewConfigurationStore.get( ConfigurationKeys.CASUAL_INBOUND_STARTUP_MODE )

        then:
        actual == expected
    }

    def "Retrieve using a List<String> key"()
    {
        given:
        List<String> expected = ["service1","service2"]

        when:
        List<String> actual = NewConfigurationStore.get( ConfigurationKeys.CASUAL_INBOUND_STARTUP_SERVICES )

        then:
        actual == expected
    }
}
