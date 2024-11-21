/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network


import io.netty.handler.logging.LogLevel
import spock.lang.Specification
import spock.lang.Unroll

class LogLevelProviderTest extends Specification
{
   @Unroll
   def "test #levelValue should return #expectedLevel"() {
      given:
      def logLevel
      logLevel = LogLevelProvider.toLogLevel(levelValue)

      expect:
      logLevel == expectedLevel

      where:
      levelValue                   || expectedLevel
      ExternalLogLevel.INFO.level  || LogLevel.INFO
      ExternalLogLevel.WARN.level  || LogLevel.WARN
      ExternalLogLevel.TRACE.level || LogLevel.TRACE
      ExternalLogLevel.DEBUG.level || LogLevel.DEBUG
      ExternalLogLevel.ERROR.level || LogLevel.ERROR
   }
}
