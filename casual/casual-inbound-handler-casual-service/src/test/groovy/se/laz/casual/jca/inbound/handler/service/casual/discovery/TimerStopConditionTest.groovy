/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.service.casual.discovery

import se.laz.casual.config.Configuration
import se.laz.casual.config.ConfigurationService
import se.laz.casual.config.Startup
import se.laz.casual.jca.RuntimeInformation
import spock.lang.Specification

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

class TimerStopConditionTest extends Specification
{
   ConfigurationService instance

   def setup()
   {
      reinitialiseConfigurationService(  )
   }

   def 'should stop, using config file'()
   {
      expect:
      RuntimeInformation.setInboundStarted(inboundStarted)
      Configuration actual
      withEnvironmentVariable( ConfigurationService.CASUAL_CONFIG_FILE_ENV_NAME, "src/test/resources/" + file )
              .execute( {
                 reinitialiseConfigurationService( )
                 actual = instance.getConfiguration(  )} )
      TimerStopCondition.of().stop( actual ) == shouldStop
      where:
      file                           || inboundStarted || shouldStop
      'casual-config-immediate.json' || true           || false
      'casual-config-trigger.json'   || false          || false
      'casual-config-trigger.json'   || true           || true
   }

   def 'should stop, config file, inbound mode override via env var'()
   {
      RuntimeInformation.setInboundStarted(inboundStarted)
      Configuration actual
      withEnvironmentVariable( ConfigurationService.CASUAL_CONFIG_FILE_ENV_NAME, "src/test/resources/" + file)
              .and(Startup.CASUAL_INBOUND_STARTUP_MODE_ENV_NAME, 'trigger')
              .execute( {
                 reinitialiseConfigurationService( )
                 actual = instance.getConfiguration(  )} )
      TimerStopCondition.of().stop( actual ) == shouldStop
      where:
      file                           || inboundStarted || shouldStop
      'casual-config-immediate.json' || true           || true
      'casual-config-trigger.json'   || false          || false
      'casual-config-trigger.json'   || true           || true
   }

   private ConfigurationService reinitialiseConfigurationService()
   {
      instance = new ConfigurationService();
   }

}
