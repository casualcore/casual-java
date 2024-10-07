/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config

import spock.lang.Specification

class ConfigurationFileReaderTest extends Specification
{

    ConfigurationStore store;

    def setup()
    {
        store = new ConfigurationStore();
    }

    def "Read file inbound startup discover, store updated"()
    {
        given:
        String file = "src/test/resources/casual-config-discover.json"

        when:
        ConfigurationFileReader.populateStoreFromFile( store, file )

        then:
        store.get( ConfigurationOptions.CASUAL_DOMAIN_NAME ) == ""
        store.get( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE ) == Mode.DISCOVER
        store.get( ConfigurationOptions.CASUAL_INBOUND_STARTUP_SERVICES ) == ["service1","service2"]
    }

}
