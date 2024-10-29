/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config

import com.github.stefanbirkner.systemlambda.SystemLambda
import spock.lang.Specification

import static se.laz.casual.config.ConfigurationDefaults.CONFIG_FILE_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.ENCODING_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.EVENT_SERVER_ENABLED_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.EVENT_SERVER_PORT_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.EXECUTOR_NUMBER_OF_THREADS_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.INBOUND_STARTUP_INITIAL_DELAY_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.INBOUND_STARTUP_MODE_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.NETTY_LOGGING_LEVEL_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.NETWORK_ENABLE_LOGHANDLER_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.SHUTDOWN_QUIET_PERIOD_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.SHUTDOWN_TIMEOUT_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.UNMANAGED_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.USE_EPOLL_DEFAULT

class ConfigurationEnvsReaderTest extends Specification
{
    ConfigurationStore store;
    ConfigurationEnvsReader instance

    def setup()
    {
        store = new ConfigurationStore()
        new ConfigurationDefaults( store ).populate(  )
        instance = new ConfigurationEnvsReader( store )
    }

    def "With config file set."()
    {
        when:
        SystemLambda.withEnvironmentVariable( ConfigurationOptions.CASUAL_CONFIG_FILE.getName(  ), value ).execute {
            instance.populateConfigFileEnv( )
        }
        String actual = store.get( ConfigurationOptions.CASUAL_CONFIG_FILE )

        then:
        actual == expected

        where:
        value         || expected
        ""            || CONFIG_FILE_DEFAULT
        "casual.json" || "casual.json"
        null          || CONFIG_FILE_DEFAULT
    }

    def "With fielded envs"()
    {
        when:
        SystemLambda.withEnvironmentVariable( ConfigurationOptions.CASUAL_API_FIELDED_ENCODING.getName(  ), encoding )
                .and( ConfigurationOptions.CASUAL_FIELD_TABLE.getName(  ), table ).execute {
            instance.populateStoreFromEnvs( )
        }
        String actualEncoding = store.get( ConfigurationOptions.CASUAL_API_FIELDED_ENCODING )
        String actualTable = store.get( ConfigurationOptions.CASUAL_FIELD_TABLE )

        then:
        actualEncoding == expectedEncoding
        actualTable == expectedTable

        where:
        encoding | table          || expectedEncoding                    | expectedTable
        "UTF8"   | "some.json"    || "UTF8"                              | "some.json"
        "latin1" | "another.json" || "latin1"                            | "another.json"
        ""       | ""             || ENCODING_DEFAULT | null
        null     | null           || ENCODING_DEFAULT | null
    }

    def "With loghander envs"()
    {
        when:
        SystemLambda.withEnvironmentVariable( ConfigurationOptions.CASUAL_NETWORK_OUTBOUND_ENABLE_LOGHANDLER.getName(  ), outbound )
                .and( ConfigurationOptions.CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER.getName(  ), inbound )
                .and( ConfigurationOptions.CASUAL_NETWORK_REVERSE_INBOUND_ENABLE_LOGHANDLER.getName(  ), reverse ).execute {
            instance.populateStoreFromEnvs( )
        }
        Boolean actualInbound = store.get( ConfigurationOptions.CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER )
        Boolean actualOutbound = store.get( ConfigurationOptions.CASUAL_NETWORK_OUTBOUND_ENABLE_LOGHANDLER )
        Boolean actualReverse = store.get( ConfigurationOptions.CASUAL_NETWORK_REVERSE_INBOUND_ENABLE_LOGHANDLER )

        then:
        actualInbound == expectedInbound
        actualOutbound == expectedOutbound
        actualReverse == expectedReverse

        where:
        inbound | outbound | reverse || expectedInbound                   | expectedOutbound                  | expectedReverse
        "false" | "false"  | "true"  || false                             | false                             | true
        "false" | "true"   | "false" || false                             | true                              | false
        "true"  | "false"  | "false" || true                              | false                             | false
        "false" | "false"  | "false" || false                             | false                             | false
        "true"  | "true"   | "true"  || true                              | true                              | true
        ""      | ""       | ""      || NETWORK_ENABLE_LOGHANDLER_DEFAULT | NETWORK_ENABLE_LOGHANDLER_DEFAULT | NETWORK_ENABLE_LOGHANDLER_DEFAULT
        " "     | " "      | " "     || NETWORK_ENABLE_LOGHANDLER_DEFAULT | NETWORK_ENABLE_LOGHANDLER_DEFAULT | NETWORK_ENABLE_LOGHANDLER_DEFAULT
        null    | null     | null    || NETWORK_ENABLE_LOGHANDLER_DEFAULT | NETWORK_ENABLE_LOGHANDLER_DEFAULT | NETWORK_ENABLE_LOGHANDLER_DEFAULT
    }

    def "With netty log levels envs"()
    {
        when:
        SystemLambda.withEnvironmentVariable( ConfigurationOptions.CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL.getName(  ), outbound )
                .and( ConfigurationOptions.CASUAL_INBOUND_NETTY_LOGGING_LEVEL.getName(  ), inbound )
                .and( ConfigurationOptions.CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL.getName(  ), reverse ).execute {
            instance.populateStoreFromEnvs( )
        }
        String actualInbound = store.get( ConfigurationOptions.CASUAL_INBOUND_NETTY_LOGGING_LEVEL )
        String actualOutbound = store.get( ConfigurationOptions.CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL )
        String actualReverse = store.get( ConfigurationOptions.CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL )

        then:
        actualInbound == expectedInbound
        actualOutbound == expectedOutbound
        actualReverse == expectedReverse

        where:
        inbound | outbound | reverse || expectedInbound             | expectedOutbound            | expectedReverse
        "ERROR" | "INFO"   | "WARN"  || "ERROR"                     | "INFO"                      | "WARN"
        "TRACE" | "DEBUG"  | "ERROR" || "TRACE"                     | "DEBUG"                     | "ERROR"
        "INFO"  | "INFO"   | "INFO"  || "INFO"                      | "INFO"                      | "INFO"
        ""      | ""       | ""      || NETTY_LOGGING_LEVEL_DEFAULT | NETTY_LOGGING_LEVEL_DEFAULT | NETTY_LOGGING_LEVEL_DEFAULT
        " "     | " "      | " "     || NETTY_LOGGING_LEVEL_DEFAULT | NETTY_LOGGING_LEVEL_DEFAULT | NETTY_LOGGING_LEVEL_DEFAULT
        null    | null     | null    || NETTY_LOGGING_LEVEL_DEFAULT | NETTY_LOGGING_LEVEL_DEFAULT | NETTY_LOGGING_LEVEL_DEFAULT
    }

    def "With inbound startup mode"()
    {
        when:
        SystemLambda.withEnvironmentVariable( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE.getName(  ), mode ).execute {
            instance.populateStoreFromEnvs( )
        }
        Mode actual = store.get( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE )

        then:
        actual == expected

        where:
        mode                     || expected
        Mode.IMMEDIATE.getName() || Mode.IMMEDIATE
        Mode.TRIGGER.getName()   || Mode.TRIGGER
        Mode.DISCOVER.getName()  || Mode.DISCOVER
        ""                       || INBOUND_STARTUP_MODE_DEFAULT
        " "                      || INBOUND_STARTUP_MODE_DEFAULT
        null                     || INBOUND_STARTUP_MODE_DEFAULT
    }

    def "With inbound startup delay"()
    {
        when:
        SystemLambda.withEnvironmentVariable( ConfigurationOptions.CASUAL_INBOUND_STARTUP_INITIAL_DELAY_SECONDS.getName(  ), delay ).execute {
            instance.populateStoreFromEnvs( )
        }
        Long actual = store.get( ConfigurationOptions.CASUAL_INBOUND_STARTUP_INITIAL_DELAY_SECONDS )

        then:
        actual == expected

        where:
        delay || expected
        "100" || 100L
        "1"   || 1L
        "15"  || 15L
        ""    || INBOUND_STARTUP_INITIAL_DELAY_DEFAULT
        " "   || INBOUND_STARTUP_INITIAL_DELAY_DEFAULT
        null  || INBOUND_STARTUP_INITIAL_DELAY_DEFAULT
    }

    def "With epoll envs"()
    {
        when:
        SystemLambda.withEnvironmentVariable( ConfigurationOptions.CASUAL_OUTBOUND_USE_EPOLL.getName(), outbound )
                .and( ConfigurationOptions.CASUAL_INBOUND_USE_EPOLL.getName(), inbound )
                .and( ConfigurationOptions.CASUAL_USE_EPOLL.getName(), epoll ).execute {
            instance.populateStoreFromEnvs( )
        }
        Boolean actualInbound = store.get( ConfigurationOptions.CASUAL_INBOUND_USE_EPOLL )
        Boolean actualOutbound = store.get( ConfigurationOptions.CASUAL_OUTBOUND_USE_EPOLL )
        Boolean actualReverse = store.get( ConfigurationOptions.CASUAL_USE_EPOLL )

        then:
        actualInbound == expectedInbound
        actualOutbound == expectedOutbound
        actualReverse == expectedEpoll

        where:
        inbound | outbound | epoll   || expectedInbound   | expectedOutbound  | expectedEpoll
        "false" | "false"  | "true"  || false             | false             | true
        "false" | "true"   | "false" || false             | true              | false
        "true"  | "false"  | "false" || true              | false             | false
        "false" | "false"  | "false" || false             | false             | false
        "true"  | "true"   | "true"  || true              | true              | true
        ""      | ""       | ""      || USE_EPOLL_DEFAULT | USE_EPOLL_DEFAULT | USE_EPOLL_DEFAULT
        " "     | " "      | " "     || USE_EPOLL_DEFAULT | USE_EPOLL_DEFAULT | USE_EPOLL_DEFAULT
        null    | null     | null    || USE_EPOLL_DEFAULT | USE_EPOLL_DEFAULT | USE_EPOLL_DEFAULT
    }

    def "With event server shutdown envs"()
    {
        when:
        SystemLambda.withEnvironmentVariable( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_QUIET_PERIOD_MILLIS.getName(), quiet )
                .and( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_TIMEOUT_MILLIS.getName(), timeout ).execute {
            instance.populateStoreFromEnvs( )
        }

        long actualQuiet = store.get( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_QUIET_PERIOD_MILLIS )
        long actualTimeout = store.get( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_TIMEOUT_MILLIS )

        then:
        actualQuiet == expectedQuiet
        actualTimeout == expectedTimeout

        where:
        quiet | timeout || expectedQuiet                                     | expectedTimeout
        "10"  | "11"    || 10L                                               | 11L
        ""    | ""      || SHUTDOWN_QUIET_PERIOD_DEFAULT | SHUTDOWN_TIMEOUT_DEFAULT
        " "   | " "     || SHUTDOWN_QUIET_PERIOD_DEFAULT | SHUTDOWN_TIMEOUT_DEFAULT
        null  | null    || SHUTDOWN_QUIET_PERIOD_DEFAULT | SHUTDOWN_TIMEOUT_DEFAULT
    }

    def "With unmanaged envs"()
    {
        when:
        SystemLambda.withEnvironmentVariable( ConfigurationOptions.CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE.getName(), poolSize )
                .and( ConfigurationOptions.CASUAL_UNMANAGED.getName(), unmanaged ).execute {
            instance.populateStoreFromEnvs( )
        }

        int actualPoolSize = store.get( ConfigurationOptions.CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE )
        boolean actualUnmanaged = store.get( ConfigurationOptions.CASUAL_UNMANAGED )

        then:
        actualPoolSize == expectedPoolSize
        actualUnmanaged == expectedUnmanaged

        where:
        poolSize | unmanaged || expectedPoolSize                                       | expectedUnmanaged
        "12"     | "true"    || 12                                                     | true
        ""       | ""        || UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE_DEFAULT | UNMANAGED_DEFAULT
        " "      | " "       || UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE_DEFAULT | UNMANAGED_DEFAULT
        null     | null      || UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE_DEFAULT | UNMANAGED_DEFAULT
    }

    def "With outbound unmanaged envs"()
    {
        when:
        SystemLambda.withEnvironmentVariable( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_NUMBER_OF_THREADS.getName(), threads )
                .and( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME.getName(), name ).execute {
            instance.populateStoreFromEnvs( )
        }

        int actualThreads = store.get( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_NUMBER_OF_THREADS )
        String actualName = store.get( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME )

        then:
        actualThreads == expectedThreads
        actualName == expectedName

        where:
        threads | name      || expectedThreads                                     | expectedName
        "12"    | "another" || 12                                                  | "another"
        ""      | ""        || EXECUTOR_NUMBER_OF_THREADS_DEFAULT | OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME_DEFAULT
        " "     | " "       || EXECUTOR_NUMBER_OF_THREADS_DEFAULT | OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME_DEFAULT
        null    | null      || EXECUTOR_NUMBER_OF_THREADS_DEFAULT | OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME_DEFAULT
    }

    def "With event server envs"()
    {
        when:
        SystemLambda.withEnvironmentVariable( ConfigurationOptions.CASUAL_EVENT_SERVER_ENABLED.getName(), enabled )
                .and( ConfigurationOptions.CASUAL_EVENT_SERVER_PORT.getName(), port )
                .and( ConfigurationOptions.CASUAL_EVENT_SERVER_USE_EPOLL.getName(), epoll ).execute {
            instance.populateStoreFromEnvs( )
        }

        boolean actualEnabled = store.get( ConfigurationOptions.CASUAL_EVENT_SERVER_ENABLED )
        int actualPort = store.get( ConfigurationOptions.CASUAL_EVENT_SERVER_PORT )
        boolean actualEpoll = store.get( ConfigurationOptions.CASUAL_EVENT_SERVER_USE_EPOLL )

        then:
        actualEnabled == expectedEnabled
        actualPort == expectedPort
        actualEpoll == expectedEpoll

        where:
        enabled | port   | epoll  || expectedEnabled              | expectedPort              | expectedEpoll
        "true"  | "1212" | "true" || true                         | 1212                      | true
        ""      | ""     | ""     || EVENT_SERVER_ENABLED_DEFAULT | EVENT_SERVER_PORT_DEFAULT | USE_EPOLL_DEFAULT
        " "     | " "    | " "    || EVENT_SERVER_ENABLED_DEFAULT | EVENT_SERVER_PORT_DEFAULT | USE_EPOLL_DEFAULT
        null    | null   | null   || EVENT_SERVER_ENABLED_DEFAULT | EVENT_SERVER_PORT_DEFAULT | USE_EPOLL_DEFAULT
    }

    def "incorrect data types that fail to cast throw CasualConfigException."()
    {
        when:
        SystemLambda.withEnvironmentVariable( option.getName(  ), value ).execute {
            instance.populateStoreFromEnvs( )
        }

        then:
        thrown ConfigurationException

        where:
        option | value
        ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE                      | "unknown"
        ConfigurationOptions.CASUAL_INBOUND_STARTUP_INITIAL_DELAY_SECONDS     | "true"
    }

}
