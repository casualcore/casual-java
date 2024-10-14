/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config

import se.laz.casual.config.json.Address
import se.laz.casual.config.json.Mode
import se.laz.casual.config.json.ReverseInbound
import spock.lang.Specification

class ConfigurationFileReaderTest extends Specification
{

    ConfigurationStore store;
    ConfigurationFileReader instance

    def setup()
    {
        store = new ConfigurationStore();
        new ConfigurationDefaults( store ).populate(  )
        instance = new ConfigurationFileReader( store )
    }

    def "Read file inbound startup discover, store updated"()
    {
        given:
        String file = "src/test/resources/casual-config-inbound-discover.json"

        when:
        instance.populateStoreFromFile( file )

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
        instance.populateStoreFromFile( file )

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
        instance.populateStoreFromFile( file )

        then:
        enabled == store.get( ConfigurationOptions.CASUAL_EVENT_SERVER_ENABLED )
        port == store.get( ConfigurationOptions.CASUAL_EVENT_SERVER_PORT )
        epoll == store.get( ConfigurationOptions.CASUAL_EVENT_SERVER_USE_EPOLL )
        quietPeriod == store.get( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_QUIET_PERIOD_MILLIS )
        timeout == store.get( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_TIMEOUT_MILLIS )

        where:
        filename                                    || enabled | port | epoll | quietPeriod | timeout
        "casual-config-empty.json"                  || false   | 7698 | false | 2000        | 15000
        "casual-config-event-server-use-epoll.json" || true    | 6699 | true  | 2000        | 15000
        "casual-config-event-server-no-epoll.json"  || true    | 9966 | false | 2000        | 15000
        "casual-config-event-server-shutdown.json"  || true    | 5987 | true  | 100         | 200
    }

    def "Read file inbound, store updated"()
    {
        given:
        String file = "src/test/resources/" + filename

        when:
        instance.populateStoreFromFile( file )

        then:
        mode == store.get( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE )
        services == store.get( ConfigurationOptions.CASUAL_INBOUND_STARTUP_SERVICES )
        epoll == store.get( ConfigurationOptions.CASUAL_INBOUND_USE_EPOLL )
        delay == store.get( ConfigurationOptions.CASUAL_INBOUND_STARTUP_INITIAL_DELAY_SECONDS )

        where:
        filename                                   || mode           | epoll | delay | services
        "casual-config-inbound-immediate.json"     || Mode.IMMEDIATE | false | 0L    | []
        "casual-config-inbound-trigger.json"       || Mode.TRIGGER   | false | 0L    | [Mode.Constants.TRIGGER_SERVICE]
        "casual-config-inbound-discover.json"      || Mode.DISCOVER  | false | 0L    | ["service1", "service2"]
        "casual-config-inbound-epoll.json"         || Mode.IMMEDIATE | true  | 0L    | []
        "casual-config-inbound-initial-delay.json" || Mode.IMMEDIATE | false | 30L   | []
    }

    def "Read file outbound, store updated"()
    {
        given:
        String file = "src/test/resources/" + filename

        when:
        instance.populateStoreFromFile( file )

        then:
        executorName == store.get( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME )
        unmanaged == store.get( ConfigurationOptions.CASUAL_OUTBOUND_UNMANAGED )
        epoll == store.get( ConfigurationOptions.CASUAL_OUTBOUND_USE_EPOLL )
        threads == store.get( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_NUMBER_OF_THREADS )

        where:
        filename                                              || executorName                                            | epoll | threads  | unmanaged
        "casual-config-outbound.json"                         || "java:comp/env/concurrent/casualManagedExecutorService" | false | 10       | false
        "casual-config-outbound-epoll.json"                   || "java:comp/DefaultManagedExecutorService"               | true  | 0        | false
        "casual-config-outbound-executorName-missing.json"    || "java:comp/DefaultManagedExecutorService"               | false | 10       | false
        "casual-config-outbound-null.json"                    || "java:comp/DefaultManagedExecutorService"               | false | 0        | false
        "casual-config-outbound-numberOfThreads-missing.json" || "java:comp/env/concurrent/casualManagedExecutorService" | false | 0        | false
        "casual-config-outbound-unmanaged.json"               || "java:comp/DefaultManagedExecutorService"               | false | 0        | true
    }

    def "Read file reverse inbound, store updated"()
    {
        given:
        String file = "src/test/resources/" + filename
        ReverseInbound expected = ReverseInbound.of( Address.of( host, port ),size, backoff )

        when:
        instance.populateStoreFromFile( file )

        List<ReverseInbound> actual = store.get( ConfigurationOptions.CASUAL_REVERSE_INBOUND_INSTANCES )

        then:
        actual == [expected]

        where:
        filename                                          || host            | port | size | backoff
        "casual-config-reverse-inbound.json"              || "10.96.186.114" | 7771 | 1    | 30000
        "casual-config-reverse-inbound-with-backoff.json" || "10.96.186.114" | 7771 | 1    | 12345
        "casual-config-reverse-inbound-with-size.json"    || "10.96.186.114" | 7771 | 42   | 30000
    }

}
