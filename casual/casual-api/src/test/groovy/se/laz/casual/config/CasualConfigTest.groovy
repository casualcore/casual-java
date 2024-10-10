/*
 * Copyright (c) 2021 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config

import se.laz.casual.api.external.json.JsonProviderFactory
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable
import static se.laz.casual.config.Shutdown.DEFAULT_QUIET_PERIOD_MILLIS
import static se.laz.casual.config.Shutdown.DEFAULT_TIMEOUT_MILLIS

class CasualConfigTest extends Specification
{
    def setup()
    {
    }

    @Unroll
    def "Get config from file"()
    {
        given:
        String config = new File( "src/test/resources/" + file ).getText( StandardCharsets.UTF_8.name(  ) )
        Configuration expected = Configuration.newBuilder()
                .withInbound( Inbound.newBuilder()
                        .withStartup( Startup.newBuilder()
                                .withMode( mode )
                                .withServices( services )
                                .build() )
                        .build() )
                .build()

        when:
        Configuration actual = JsonProviderFactory.getJsonProvider(  ).fromJson( new StringReader( config ), Configuration.class )

        then:
        actual == expected

        where:
        file                           || mode           | services
        "casual-config-inbound-immediate.json" || Mode.IMMEDIATE | []
        "casual-config-inbound-trigger.json"   || Mode.TRIGGER   | [Mode.Constants.TRIGGER_SERVICE]
        "casual-config-discover.json"  || Mode.DISCOVER  | ["service1", "service2"]
    }

   @Unroll
   def "inbound startup config also set in env, configuration from file should be used"()
   {
      given:
      String config = new File( "src/test/resources/" + file ).getText( StandardCharsets.UTF_8.name(  ) )
      Configuration expected = Configuration.newBuilder()
              .withInbound( Inbound.newBuilder()
                      .withStartup( Startup.newBuilder()
                              .withMode( mode )
                              .withServices( services )
                              .build() )
                      .build() )
              .build()

      when:
      Configuration actual
      withEnvironmentVariable(Inbound.CASUAL_INBOUND_STARTUP_MODE, modeFromEnv).execute( {
          actual = JsonProviderFactory.getJsonProvider(  ).fromJson( new StringReader( config ), Configuration.class )
      })

      then:
      withEnvironmentVariable(Inbound.CASUAL_INBOUND_STARTUP_MODE, modeFromEnv).execute({
         actual == expected
      })
      where:
      file                           || mode           || modeFromEnv           | services
      "casual-config-inbound-immediate.json" || Mode.IMMEDIATE || Mode.TRIGGER.name     | []
      "casual-config-inbound-trigger.json"   || Mode.TRIGGER   || Mode.IMMEDIATE.name   | [Mode.Constants.TRIGGER_SERVICE]
      "casual-config-discover.json"  || Mode.DISCOVER  || Mode.TRIGGER.name     | ["service1", "service2"]
   }

   def 'startup missing from configuration, mode should be set according to env var'()
   {
      given:
      String config = new File( "src/test/resources/" + file ).getText( StandardCharsets.UTF_8.name(  ) )
      Configuration expected = Configuration.newBuilder()
              .withInbound( Inbound.newBuilder()
                      .withStartup( Startup.newBuilder()
                              .withMode( mode )
                              .withServices( services )
                              .build() )
                      .build() )
              .build()

      when:
      Configuration actual
      withEnvironmentVariable(Inbound.CASUAL_INBOUND_STARTUP_MODE, modeFromEnv).execute( {
         actual = JsonProviderFactory.getJsonProvider(  ).fromJson( new StringReader( config ), Configuration.class )
      })

      then:
      withEnvironmentVariable(Inbound.CASUAL_INBOUND_STARTUP_MODE, modeFromEnv).execute({
         actual == expected
      })
      !actual.getEventServer().isPresent()
      where:
      file                           || mode           || modeFromEnv           | services
      "casual-config-empty.json"     || Mode.TRIGGER   || Mode.TRIGGER.name     | []
   }

    @Unroll
    def "Get config from file default Domain"()
    {
        given:
        String config = new File( "src/test/resources/"+file).getText( StandardCharsets.UTF_8.name(  ) )
        Configuration expected = Configuration.newBuilder()
                .withDomain(Domain.of(domainName))
                .build()
        when:
        Configuration actual = JsonProviderFactory.getJsonProvider(  ).fromJson( new StringReader( config ), Configuration.class )

        then:
        actual == expected

        where:
        file                                          || domainName
        "casual-config-domain-info.json"              || "casual-java-test"
        "casual-config-domain-info-missing.json"      || ""
        "casual-config-domain-info-null.json"         || ""
        "casual-config-domain-info-name-missing.json" || ""
    }

   @Unroll
   def "Get config from file default Domain, env variable set"()
   {
      given:
      String config = new File( "src/test/resources/"+file).getText( StandardCharsets.UTF_8.name(  ) )
      Configuration expected = Configuration.newBuilder()
              .withDomain(Domain.of(domainName))
              .build()
      when:

      Configuration actual
      withEnvironmentVariable(Domain.DOMAIN_NAME_ENV, domainName).execute( {
         actual = JsonProviderFactory.getJsonProvider(  ).fromJson( new StringReader( config ), Configuration.class )
      } )

      then:
      withEnvironmentVariable(Domain.DOMAIN_NAME_ENV, domainName).execute({
         actual == expected
      })
      when:
      actual = JsonProviderFactory.getJsonProvider(  ).fromJson( new StringReader( config ), Configuration.class )
      then:
      actual.getDomain().getName().isEmpty()
      where:
      file                                          || domainName
      "casual-config-domain-info-missing.json"      || "Domain A"
      "casual-config-domain-info-null.json"         || "Domain B"
      "casual-config-domain-info-name-missing.json" || "Domain C"
   }

   def "Get config from file Domain set in configuration and env variable set - the configuration should be used"()
   {
      given:
      String config = new File( "src/test/resources/"+file).getText( StandardCharsets.UTF_8.name(  ) )
      Configuration expected = Configuration.newBuilder()
              .withDomain(Domain.of(domainName))
              .build()
      when:
      Configuration actual
      withEnvironmentVariable(Domain.DOMAIN_NAME_ENV, envDomainName).execute( {
         actual = JsonProviderFactory.getJsonProvider(  ).fromJson( new StringReader( config ), Configuration.class )
      } )

      then:
      withEnvironmentVariable(Domain.DOMAIN_NAME_ENV, envDomainName).execute({
         actual == expected
      })
      where:
      file                                          || domainName          || envDomainName
      "casual-config-domain-info.json"              || "casual-java-test"  || 'env-domain-name'
   }

   @Unroll
   def "Event server configuration"()
   {
      given:
      String config = new File( "src/test/resources/" + file ).getText( StandardCharsets.UTF_8.name(  ) )
      Configuration expected = Configuration.newBuilder()
              .withEventServer(EventServer.createBuilder()
                      .withPortNumber(port)
                      .withUseEpoll(epoll)
                      .withShutdown( Shutdown.newBuilder()
                              .withQuietPeriod(quietPeriod)
                              .withTimeout(timeout)
                              .build())
                      .build())
              .build()

      when:
      Configuration actual = JsonProviderFactory.getJsonProvider(  ).fromJson( new StringReader( config ), Configuration.class )

      then:
      actual == expected

      where:
      file                                        || port | epoll | quietPeriod                 | timeout
      'casual-config-event-server-use-epoll.json' || 6699 | true  | DEFAULT_QUIET_PERIOD_MILLIS | DEFAULT_TIMEOUT_MILLIS
      'casual-config-event-server-no-epoll.json'  || 9966 | false | DEFAULT_QUIET_PERIOD_MILLIS | DEFAULT_TIMEOUT_MILLIS
      'casual-config-event-server-shutdown.json'  || 5987 | true  | 100                         | 200

   }

}
