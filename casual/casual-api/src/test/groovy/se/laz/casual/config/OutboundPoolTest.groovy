package se.laz.casual.config


import spock.lang.Specification

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

class OutboundPoolTest extends Specification
{
   def 'no pool config'()
   {
      given:
      Configuration actual
      withEnvironmentVariable( ConfigurationService.CASUAL_CONFIG_FILE_ENV_NAME, "src/test/resources/casual-config-outbound-unmanaged.json")
              .execute( {
                 ConfigurationService instance = new ConfigurationService()
                 actual = instance.getConfiguration(  )} )
      when:
      Integer poolSize = actual.getOutbound().getPools().stream()
              .filter({it.getName() == "does-not-exist"} )
              .map({it.getSize()})
              .findFirst()
              .orElse(null)
      then:
      poolSize == null
   }

   def 'with pool configurations'()
   {
      given:
      def bigPoolName = 'big-pool'
      def smallPoolName = 'small-pool'
      Configuration actual
      withEnvironmentVariable( ConfigurationService.CASUAL_CONFIG_FILE_ENV_NAME, "src/test/resources/casual-config-outbound-network-pooling.json")
              .execute( {
                 ConfigurationService instance = new ConfigurationService()
                 actual = instance.getConfiguration(  )} )
      when:
      Integer poolSize = actual.getOutbound().getPools().stream()
              .filter({it.getName() == bigPoolName} )
              .map({it.getSize()})
              .findFirst()
              .orElse(null)
      then:
      poolSize == 42
      when:
      poolSize = actual.getOutbound().getPools().stream()
              .filter({it.getName() == smallPoolName} )
              .map({it.getSize()})
              .findFirst()
              .orElse(null)
      then:
      poolSize == 2
      when: // does not exist
      poolSize = actual.getOutbound().getPools().stream()
              .filter({it.getName() == "no-name"} )
              .map({it.getSize()})
              .findFirst()
              .orElse(null)
      then:
      null == poolSize
   }

}
