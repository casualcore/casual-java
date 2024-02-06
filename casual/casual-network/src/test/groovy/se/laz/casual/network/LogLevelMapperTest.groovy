/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network

import spock.lang.Specification
import spock.lang.Unroll
import io.netty.handler.logging.LogLevel

class LogLevelMapperTest extends Specification
{
   @Unroll
   def "test map with #externalLogLevel should return #expectedLevel"() {
      given:
      def logLevel = LogLevelMapper.map(externalLogLevel)

      expect:
      logLevel == expectedLevel

      where:
      externalLogLevel          || expectedLevel
      ExternalLogLevel.INFO     || LogLevel.INFO
      ExternalLogLevel.WARN     || LogLevel.WARN
      ExternalLogLevel.TRACE    || LogLevel.TRACE
      ExternalLogLevel.DEBUG    || LogLevel.DEBUG
      ExternalLogLevel.ERROR    || LogLevel.ERROR
   }
}
