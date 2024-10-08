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

    def "Read file domain info, store updated"()
    {
        given:
        String file = "src/test/resources/" + filename

        when:
        ConfigurationFileReader.populateStoreFromFile( store, file )

        then:
        store.get( ConfigurationOptions.CASUAL_DOMAIN_NAME ) == expected

        where:
        filename                                      || expected
        "casual-config-domain-info.json"              || "casual-java-test"
        "casual-config-domain-info-missing.json"      || ""
        "casual-config-domain-info-name-missing.json" || ""
        "casual-config-domain-info-null.json"         || ""
    }
    
    def "Read file event server, store updated"()
    {
        given:
        String file = "src/test/resources/" + filename

        when:
        ConfigurationFileReader.populateStoreFromFile( store, file )

        then:
        port == store.get( ConfigurationOptions.CASUAL_EVENT_SERVER_PORT )
        epoll == store.get( ConfigurationOptions.CASUAL_EVENT_SERVER_USE_EPOLL )
        quietPeriod == store.get( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_QUIET_PERIOD_MILLIS )
        timeout == store.get( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_TIMEOUT_MILLIS )

        where:
        filename                                    || port | epoll | quietPeriod | timeout
        "casual-config-event-server-use-epoll.json" || 6699 | true  | 2000        | 15000
        "casual-config-event-server-no-epoll.json"  || 9966 | false | 2000        | 15000
        "casual-config-event-server-shutdown.json"  || 5987 | true  | 100         | 200
    }

}
