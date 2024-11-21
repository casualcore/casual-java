/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json

import se.laz.casual.config.ConfigurationDefaults
import se.laz.casual.config.ConfigurationOptions
import se.laz.casual.config.ConfigurationStore
import spock.lang.Specification

import static se.laz.casual.config.ConfigurationDefaults.DOMAIN_NAME_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.EVENT_SERVER_ENABLED_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.EVENT_SERVER_PORT_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.EXECUTOR_NUMBER_OF_THREADS_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.INBOUND_STARTUP_INITIAL_DELAY_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.INBOUND_STARTUP_MODE_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.INBOUND_START_SERVICES_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.REVERSE_INBOUND_CONNECTION_MAX_BACKOFF_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.REVERSE_INBOUND_CONNECTION_POOL_SIZE_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.SHUTDOWN_QUIET_PERIOD_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.SHUTDOWN_TIMEOUT_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.UNMANAGED_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.USE_EPOLL_DEFAULT

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
        store.get( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE ) == se.laz.casual.config.Mode.DISCOVER
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
        "casual-config-domain-info-missing.json"      || DOMAIN_NAME_DEFAULT
        "casual-config-domain-info-name-missing.json" || DOMAIN_NAME_DEFAULT
        "casual-config-domain-info-null.json"         || DOMAIN_NAME_DEFAULT
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
        filename                                    || enabled                      | port                      | epoll             | quietPeriod                   | timeout
        "casual-config-empty.json"                  || EVENT_SERVER_ENABLED_DEFAULT | EVENT_SERVER_PORT_DEFAULT | USE_EPOLL_DEFAULT | SHUTDOWN_QUIET_PERIOD_DEFAULT | SHUTDOWN_TIMEOUT_DEFAULT
        "casual-config-event-server-use-epoll.json" || true                         | 6699                      | true              | SHUTDOWN_QUIET_PERIOD_DEFAULT | SHUTDOWN_TIMEOUT_DEFAULT
        "casual-config-event-server-no-epoll.json"  || true                         | 9966                      | USE_EPOLL_DEFAULT | SHUTDOWN_QUIET_PERIOD_DEFAULT | SHUTDOWN_TIMEOUT_DEFAULT
        "casual-config-event-server-shutdown.json"  || true                         | 5987                      | true              | 100                           | 200
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
        filename                                   || mode                                | epoll             | delay                                 | services
        "casual-config-inbound-immediate.json"     || se.laz.casual.config.Mode.IMMEDIATE | USE_EPOLL_DEFAULT | INBOUND_STARTUP_INITIAL_DELAY_DEFAULT | INBOUND_START_SERVICES_DEFAULT
        "casual-config-inbound-trigger.json"       || se.laz.casual.config.Mode.TRIGGER   | USE_EPOLL_DEFAULT | INBOUND_STARTUP_INITIAL_DELAY_DEFAULT | INBOUND_START_SERVICES_DEFAULT
        "casual-config-inbound-discover.json"      || se.laz.casual.config.Mode.DISCOVER  | USE_EPOLL_DEFAULT | INBOUND_STARTUP_INITIAL_DELAY_DEFAULT | ["service1", "service2"]
        "casual-config-inbound-epoll.json"         || INBOUND_STARTUP_MODE_DEFAULT        | true              | INBOUND_STARTUP_INITIAL_DELAY_DEFAULT | INBOUND_START_SERVICES_DEFAULT
        "casual-config-inbound-initial-delay.json" || INBOUND_STARTUP_MODE_DEFAULT        | USE_EPOLL_DEFAULT | 30L                                   | INBOUND_START_SERVICES_DEFAULT
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
        filename                                              || executorName                                            | epoll             | threads                            | unmanaged
        "casual-config-outbound.json"                         || "java:comp/env/concurrent/casualManagedExecutorService" | USE_EPOLL_DEFAULT | 10                                 | UNMANAGED_DEFAULT
        "casual-config-outbound-epoll.json"                   || OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME_DEFAULT          | true              | EXECUTOR_NUMBER_OF_THREADS_DEFAULT | UNMANAGED_DEFAULT
        "casual-config-outbound-executorName-missing.json"    || OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME_DEFAULT          | USE_EPOLL_DEFAULT | 10                                 | UNMANAGED_DEFAULT
        "casual-config-outbound-null.json"                    || OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME_DEFAULT          | USE_EPOLL_DEFAULT | EXECUTOR_NUMBER_OF_THREADS_DEFAULT | UNMANAGED_DEFAULT
        "casual-config-outbound-numberOfThreads-missing.json" || "java:comp/env/concurrent/casualManagedExecutorService" | USE_EPOLL_DEFAULT | EXECUTOR_NUMBER_OF_THREADS_DEFAULT | UNMANAGED_DEFAULT
        "casual-config-outbound-unmanaged.json"               || OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME_DEFAULT          | USE_EPOLL_DEFAULT | EXECUTOR_NUMBER_OF_THREADS_DEFAULT | true
    }

    def "Read file reverse inbound, store updated"()
    {
        given:
        String file = "src/test/resources/" + filename
        se.laz.casual.config.ReverseInbound expected = se.laz.casual.config.ReverseInbound.newBuilder()
                .withHost( host )
                .withPort( port )
                .withSize( size )
                .withMaxConnectionBackoffMillis( backoff )
                .build()

        when:
        instance.populateStoreFromFile( file )

        List<se.laz.casual.config.ReverseInbound> actual = store.get( ConfigurationOptions.CASUAL_REVERSE_INBOUND_INSTANCES )

        then:
        actual == [expected]

        where:
        filename                                          || host            | port | size                                         | backoff
        "casual-config-reverse-inbound.json"              || "10.96.186.114" | 7771 | REVERSE_INBOUND_CONNECTION_POOL_SIZE_DEFAULT | REVERSE_INBOUND_CONNECTION_MAX_BACKOFF_DEFAULT
        "casual-config-reverse-inbound-with-backoff.json" || "10.96.186.114" | 7771 | REVERSE_INBOUND_CONNECTION_POOL_SIZE_DEFAULT | 12345
        "casual-config-reverse-inbound-with-size.json"    || "10.96.186.114" | 7771 | 42                                           | REVERSE_INBOUND_CONNECTION_MAX_BACKOFF_DEFAULT
    }

}
