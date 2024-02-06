/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network

import com.github.stefanbirkner.systemlambda.SystemLambda
import io.netty.handler.logging.LogLevel
import spock.lang.Specification
import spock.lang.Unroll

class LogLevelProviderTest extends Specification
{
   @Unroll
   def "test getOrDefault with #envVarName set to #envVarValue should return #expectedLevel"() {
      given:
      def logLevel
      SystemLambda.withEnvironmentVariable(envVarName, envVarValue).execute {
         logLevel = LogLevelProvider.getOrDefault(envVarName)
      }

      expect:
      logLevel == expectedLevel

      where:
      // note: only need to test the entire set - once
      envVarName                                     || envVarValue                        || expectedLevel
      'CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL'   || null                               || LogLevel.INFO
      'CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL'   || ""                                 || LogLevel.INFO
      'CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL'   || ExternalLogLevel.INFO.level        || LogLevel.INFO
      'CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL'   || ExternalLogLevel.WARN.level        || LogLevel.WARN
      'CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL'   || ExternalLogLevel.TRACE.level       || LogLevel.TRACE
      'CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL'   || ExternalLogLevel.DEBUG.level       || LogLevel.DEBUG
      'CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL'   || ExternalLogLevel.ERROR.level       || LogLevel.ERROR
   }
}
