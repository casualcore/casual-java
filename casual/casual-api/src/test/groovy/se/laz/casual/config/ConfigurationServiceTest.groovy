/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config


import spock.lang.Specification
import spock.lang.Unroll

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

class ConfigurationServiceTest extends Specification
{
    ConfigurationService instance

    def setup()
    {
        instance = new ConfigurationService()
    }

    def "Get configuration with no file or envs returns default empty config."()
    {
        given:
        Configuration expected = Configuration.newBuilder(  ).build(  )

        when:
        Configuration actual = instance.getConfiguration()

        then:
        actual == expected
    }

    @Unroll
    def "Get configuration where file is provided, returns matching configuration."()
    {
        given:
        Configuration expected = Configuration.newBuilder(  )
                    .withInbound( Inbound.newBuilder(  )
                            .withStartup( Startup.newBuilder(  )
                                    .withMode( mode )
                                    .withServices( services )
                                    .build(  ) )
                            .build(  ) )
                .build(  )

        when:
        Configuration actual
        withEnvironmentVariable( ConfigurationService.CASUAL_CONFIG_FILE_ENV_NAME, "src/test/resources/" + file )
                .execute( {
                    instance = new ConfigurationService()
                    actual = instance.getConfiguration(  )} )

        then:
        actual == expected

        where:
        file                           || mode           | services
        "casual-config-immediate.json" || Mode.IMMEDIATE | []
        "casual-config-trigger.json"   || Mode.TRIGGER   | [Mode.Constants.TRIGGER_SERVICE]
        "casual-config-discover.json"  || Mode.DISCOVER  | ["service1", "service2"]
    }

    @Unroll
    def "outbound config #executorName, #numberOfThreads #unmanaged #useEpoll"()
    {
        given:
        Configuration expected = Configuration.newBuilder()
                .withOutbound(Outbound.newBuilder()
                        .withManagedExecutorServiceName(executorName)
                        .withNumberOfThreads(numberOfThreads)
                        .withUnmanaged(unmanaged)
                        .withUseEpoll(useEpoll)
                        .build())
                .build()

        when:
        Configuration actual
        withEnvironmentVariable( ConfigurationService.CASUAL_CONFIG_FILE_ENV_NAME, "src/test/resources/" + file )
                .execute( {
                    instance = new ConfigurationService()
                    actual = instance.getConfiguration(  )} )

        then:
        actual == expected

        where:
        file                                                  || executorName                                                        || numberOfThreads || unmanaged || useEpoll
        'casual-config-outbound.json'                         || 'java:comp/env/concurrent/casualManagedExecutorService'             || 10              || false     || false
        'casual-config-outbound-executorName-missing.json'    || 'java:comp/DefaultManagedExecutorService'                           || 10              || false     || false
        'casual-config-outbound-numberOfThreads-missing.json' || 'java:comp/env/concurrent/casualManagedExecutorService'             || 0               || false     || false
        'casual-config-outbound-null.json'                    || 'java:comp/DefaultManagedExecutorService'                           || 0               || false     || false
        'casual-config-outbound-unmanaged.json'               || 'java:comp/DefaultManagedExecutorService'                           || 0               || true      || false
        'casual-config-outbound-epoll.json'                   || 'java:comp/DefaultManagedExecutorService'                           || 0               || false     || true
    }

    def 'default outbound config, no file'()
    {
        given:
        Configuration expected = Configuration.newBuilder()
                .withOutbound(Outbound.newBuilder().build())
                .build()
        when:
        Configuration actual = ConfigurationService.getInstance().getConfiguration()
        then:
        actual == expected
    }

   def 'no outbound config, useEpoll set via env var'()
   {
      given:
      // Outbound.of('java:comp/DefaultManagedExecutorService', 0, false, true)
      Configuration expected = Configuration.newBuilder()
              .withOutbound(Outbound.newBuilder()
                      .withManagedExecutorServiceName('java:comp/DefaultManagedExecutorService')
                      .withNumberOfThreads(0)
                      .withUnmanaged(false)
                      .withUseEpoll(true)
                      .build())
              .build()
      when:
      Configuration actual
      withEnvironmentVariable( Outbound.USE_EPOLL_ENV_VAR_NAME, "true" )
              .execute( {
                 actual = ConfigurationService.getInstance().getConfiguration()} )
      then:
      withEnvironmentVariable( Outbound.USE_EPOLL_ENV_VAR_NAME, "true" )
              .execute( {
                 actual == expected
              } )
   }

    def "Get configuration where file not found, throws CasualRuntimeException."()
    {
        when:
        Configuration actual
        withEnvironmentVariable( ConfigurationService.CASUAL_CONFIG_FILE_ENV_NAME, "invalid.json" )
                .execute( {
                    instance = new ConfigurationService()
                    actual = instance.getConfiguration(  )} )

        then:
        thrown ConfigurationException
    }

    @Unroll
    def "Get configuration where file is not provided but mode is, returns matching configuration."()
    {
        given:
        Configuration expected = Configuration.newBuilder(  )
                .withInbound( Inbound.newBuilder(  )
                        .withStartup( Startup.newBuilder(  )
                                .withMode( mode )
                                .withServices( services )
                                .build(  ) )
                        .build(  ) )
                .build(  )

        when:
        Configuration actual
        withEnvironmentVariable( ConfigurationService.CASUAL_INBOUND_STARTUP_MODE_ENV_NAME, env )
                .execute( {
                    instance = new ConfigurationService()
                    actual = instance.getConfiguration(  )} )

        then:
        actual == expected

        where:
        env                      || mode           | services
        Mode.Constants.IMMEDIATE || Mode.IMMEDIATE | []
        Mode.Constants.TRIGGER   || Mode.TRIGGER   | [Mode.Constants.TRIGGER_SERVICE]
        Mode.Constants.DISCOVER  || Mode.DISCOVER  | []
        // This test is for when the env var is set such as FOO=
        // When reading it with System.getEnv that then is returned as the empty string as opposed to null if the env var
        // was not set at all - the expected behaviour in this case is that the default mode is used
        ''                       || Mode.IMMEDIATE | []
    }

   @Unroll
   def "reverse inbound config #host, #port, #size"()
   {
      given:
      Configuration expected = Configuration.newBuilder()
              .withReverseInbound(ReverseInbound.of(Address.of(host, port), size))
              .build()

      when:
      Configuration actual
      withEnvironmentVariable(ConfigurationService.CASUAL_CONFIG_FILE_ENV_NAME, "src/test/resources/" + file)
              .execute({
                 instance = new ConfigurationService()
                 actual = instance.getConfiguration()
              })

      then:
      actual == expected

      where:
      file                                                       || host              || port || size
      'casual-config-reverse-inbound.json'                       || '10.96.186.114'   || 7771 || 1
      'casual-config-reverse-inbound-with-size.json'             || '10.96.186.114'   || 7771 || 42
   }

}
