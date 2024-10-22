/*
 * Copyright (c) 2021 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config


import spock.lang.Specification
import spock.lang.Unroll

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

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
        file                                       || mode           | services                         | epoll | delay
        "casual-config-inbound-immediate.json"     || Mode.IMMEDIATE | []                               | false | 0
        "casual-config-inbound-trigger.json"       || Mode.TRIGGER   | [Mode.Constants.TRIGGER_SERVICE] | false | 0
        "casual-config-inbound-discover.json"      || Mode.DISCOVER  | ["service1", "service2"]         | false | 0
        "casual-config-inbound-epoll.json"         || Mode.IMMEDIATE | []                               | true  | 0
        "casual-config-inbound-initial-delay.json" || Mode.IMMEDIATE | []                               | false | 30
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
        env                      || mode           | services
        Mode.Constants.IMMEDIATE || Mode.IMMEDIATE | []
        Mode.Constants.TRIGGER   || Mode.TRIGGER   | [Mode.Constants.TRIGGER_SERVICE] //TODO fix this.
        Mode.Constants.DISCOVER  || Mode.DISCOVER  | []
        // This test is for when the env var is set such as FOO=
        // When reading it with System.getEnv that then is returned as the empty string as opposed to null if the env var
        // was not set at all - the expected behaviour in this case is that the default mode is used
        ''                       || Mode.IMMEDIATE | []
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
}
