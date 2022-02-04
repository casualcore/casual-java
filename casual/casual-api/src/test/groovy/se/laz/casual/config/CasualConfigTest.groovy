/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config

import se.laz.casual.api.external.json.JsonProviderFactory
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

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
        "casual-config-immediate.json" || Mode.IMMEDIATE | []
        "casual-config-trigger.json"   || Mode.TRIGGER   | [Mode.Constants.TRIGGER_SERVICE]
        "casual-config-discover.json"  || Mode.DISCOVER  | ["service1", "service2"]
    }

   @Unroll
   def "inbound statup config also set in env, configuration from file should be used"()
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
      "casual-config-immediate.json" || Mode.IMMEDIATE || Mode.TRIGGER.name     | []
      "casual-config-trigger.json"   || Mode.TRIGGER   || Mode.IMMEDIATE.name   | [Mode.Constants.TRIGGER_SERVICE]
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

}
