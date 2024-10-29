/*
 * Copyright (c) 2021 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config

import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.Supplier

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable
import static se.laz.casual.config.ConfigurationDefaults.ENCODING_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.INBOUND_STARTUP_INITIAL_DELAY_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.INBOUND_STARTUP_MODE_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.INBOUND_START_SERVICES_DEFAULT
import static se.laz.casual.config.ConfigurationDefaults.USE_EPOLL_DEFAULT

class ConfigurationServiceTest extends Specification
{

    def cleanup()
    {
        ConfigurationService.reload(  )
    }

    def "Get configuration with no file or envs returns default empty config."()
    {
        given:
        String expected = ""

        when:
        String actual = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_DOMAIN_NAME )

        then:
        actual == expected
    }

    @Unroll
    def "Get configuration where only file env is provided, no other envs, returns matching configuration."()
    {
        when:
        withEnvironmentVariable( ConfigurationOptions.CASUAL_CONFIG_FILE.getName(  ), "src/test/resources/" + file )
                .execute( {
                    ConfigurationService.reload( )
                } )

        then:
        mode == ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE )
        services == ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_INBOUND_STARTUP_SERVICES )
        epoll == ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_INBOUND_USE_EPOLL )
        delay == ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_INBOUND_STARTUP_INITIAL_DELAY_SECONDS )

        where:
        file                                       || mode                         | services                         | epoll             | delay
        "casual-config-inbound-immediate.json"     || Mode.IMMEDIATE               | INBOUND_START_SERVICES_DEFAULT   | USE_EPOLL_DEFAULT | INBOUND_STARTUP_INITIAL_DELAY_DEFAULT
        "casual-config-inbound-trigger.json"       || Mode.TRIGGER                 | [Mode.Constants.TRIGGER_SERVICE] | USE_EPOLL_DEFAULT | INBOUND_STARTUP_INITIAL_DELAY_DEFAULT
        "casual-config-inbound-discover.json"      || Mode.DISCOVER                | ["service1", "service2"]         | USE_EPOLL_DEFAULT | INBOUND_STARTUP_INITIAL_DELAY_DEFAULT
        "casual-config-inbound-epoll.json"         || INBOUND_STARTUP_MODE_DEFAULT | INBOUND_START_SERVICES_DEFAULT   | true              | INBOUND_STARTUP_INITIAL_DELAY_DEFAULT
        "casual-config-inbound-initial-delay.json" || INBOUND_STARTUP_MODE_DEFAULT | INBOUND_START_SERVICES_DEFAULT   | USE_EPOLL_DEFAULT | 30
    }

   def 'no outbound config, useEpoll set via env var'()
   {
      when:
      withEnvironmentVariable( ConfigurationOptions.CASUAL_USE_EPOLL.getName(  ), "true" )
              .execute( {
                  ConfigurationService.reload(  )
              } )
      boolean epoll = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_USE_EPOLL )

      then:
      epoll
   }

    def "Get configuration where file not found, throws CasualRuntimeException."()
    {
        when:
        withEnvironmentVariable( ConfigurationOptions.CASUAL_CONFIG_FILE.getName(  ), "invalid.json" )
                .execute( {
                    ConfigurationService.reload(  )
                } )

        then:
        thrown ConfigurationException
    }

    @Unroll
    def "Get configuration where file empty and env mode is provided, returns matching configuration."()
    {
        when:
        withEnvironmentVariable( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE.getName(  ), env )
                .and( ConfigurationOptions.CASUAL_CONFIG_FILE.getName(  ), "src/test/resources/casual-config-empty.json" )
                .execute( {
                    ConfigurationService.reload( )
                } )

        Mode actualMode = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE )
        List<String> actualServices = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_INBOUND_STARTUP_SERVICES )

        then:
        actualMode == mode
        actualServices == services

        where:
        env                      || mode                         | services
        Mode.Constants.IMMEDIATE || Mode.IMMEDIATE               | []
        Mode.Constants.TRIGGER   || Mode.TRIGGER                 | [Mode.Constants.TRIGGER_SERVICE]
        Mode.Constants.DISCOVER  || Mode.DISCOVER                | []
        ''                       || INBOUND_STARTUP_MODE_DEFAULT | []
    }

    def "Unmanaged confusion for default and settings."()
    {
        given:
        withEnvironmentVariable( ConfigurationOptions.CASUAL_UNMANAGED.getName(  ), unmanaged )
                .and( ConfigurationOptions.CASUAL_CONFIG_FILE.getName(  ), "src/test/resources/" + file )
                .execute( {
                    ConfigurationService.reload( )
                } )

        when:
        Boolean actualRootUnmanaged = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_UNMANAGED )
        Boolean actualOutboundUnmanaged = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_OUTBOUND_UNMANAGED )

        then:
        actualRootUnmanaged == expectedRootUnmanaged
        actualOutboundUnmanaged == expectedOutboundUnmanaged

        where:
        file                                          | unmanaged | expectedRootUnmanaged | expectedOutboundUnmanaged
        "casual-config-empty.json"                    | ""        | false                 | false
        "casual-config-empty.json"                    | "false"   | false                 | false
        "casual-config-empty.json"                    | "true"    | true                  | true
        "casual-config-outbound.json"                 | ""        | false                 | false
        "casual-config-outbound.json"                 | "false"   | false                 | false
        "casual-config-outbound.json"                 | "true"    | true                  | true
        "casual-config-outbound-unmanaged.json"       | ""        | true                  | true
        "casual-config-outbound-unmanaged.json"       | "false"   | false                 | false
        "casual-config-outbound-unmanaged.json"       | "true"    | true                  | true
        "casual-config-outbound-unmanaged-false.json" | ""        | false                 | false
        "casual-config-outbound-unmanaged-false.json" | "false"   | false                 | false
        "casual-config-outbound-unmanaged-false.json" | "true"    | true                  | true

    }

    def "Set configuration value dynamically, e.g. for testing."()
    {
        given:
        String expected = "new value"

        when:
        ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_CONFIG_FILE, expected )
        String actual = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_CONFIG_FILE )

        then:
        actual == expected
    }

    def "Static options for put and get."()
    {
        given:
        boolean expected = true
        ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_EVENT_SERVER_ENABLED, expected )

        when:
        boolean actual = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_EVENT_SERVER_ENABLED )

        then:
        actual == expected
    }

    def "Reload configurations."()
    {
        given:
        String initial = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_API_FIELDED_ENCODING )
        String expected = "latin1"
        ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_API_FIELDED_ENCODING, expected )

        when:
        String actual = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_API_FIELDED_ENCODING )

        then:
        actual == expected

        when:
        ConfigurationService.reload()
        actual = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_API_FIELDED_ENCODING )

        then:
        actual == initial
    }

    def "Log configuration."()
    {
        when:
        Supplier<String> supplier = ConfigurationService.log( )
        String actual = supplier.get(  )

        then:
        actual.contains( ENCODING_DEFAULT )

        when:
        String encoding = "latin1"
        ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_API_FIELDED_ENCODING, encoding )
        supplier = ConfigurationService.log( )
        actual = supplier.get(  )

        then:
        actual.contains( encoding )
    }
}
