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
              .filter({it.getAddress().getHost() == "10.96.186.114" && it.getAddress().getPort() == 7771} )
              .map({it.getSize()})
              .findFirst()
              .orElse(null)
      then:
      poolSize == null
   }

   def 'with pool configurations'()
   {
      given:
      Address addressOne = Address.of('10.96.186.114', 7771)
      Address addressTwo = Address.of('casual-one', 7771)
      Configuration actual
      withEnvironmentVariable( ConfigurationService.CASUAL_CONFIG_FILE_ENV_NAME, "src/test/resources/casual-config-outbound-network-pooling.json")
              .execute( {
                 ConfigurationService instance = new ConfigurationService()
                 actual = instance.getConfiguration(  )} )
      when:
      Integer poolSize = actual.getOutbound().getPools().stream()
              .filter({it.getAddress() == addressOne} )
              .map({it.getSize()})
              .findFirst()
              .orElse(null)
      then:
      poolSize == 42
      when:
      poolSize = actual.getOutbound().getPools().stream()
              .filter({it.getAddress() == addressTwo} )
              .map({it.getSize()})
              .findFirst()
              .orElse(null)
      then:
      poolSize == 2
      when: // does not exist
      poolSize = actual.getOutbound().getPools().stream()
              .filter({it.getAddress().getHost() == "no-host"} )
              .map({it.getSize()})
              .findFirst()
              .orElse(null)
      then:
      null == poolSize
   }

}
