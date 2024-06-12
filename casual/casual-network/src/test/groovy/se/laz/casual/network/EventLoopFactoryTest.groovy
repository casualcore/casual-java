/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network

import se.laz.casual.config.Configuration
import spock.lang.Specification

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

class EventLoopFactoryTest extends Specification
{
   def 'correct instances are returned'()
   {
      given:
      def outboundEventLoopGroup
      def reverseEventLoopGroup
      when:
      withEnvironmentVariable( Configuration.UNMANAGED_ENV_VAR_NAME, "true" )
              .execute( {
                 outboundEventLoopGroup = EventLoopFactory.getInstance(EventLoopClient.OUTBOUND)
                 reverseEventLoopGroup = EventLoopFactory.getInstance(EventLoopClient.REVERSE)
              } )
      then:
      withEnvironmentVariable( Configuration.UNMANAGED_ENV_VAR_NAME, "true" )
              .execute({
                 outboundEventLoopGroup != reverseEventLoopGroup
                 outboundEventLoopGroup == EventLoopFactory.getInstance(EventLoopClient.OUTBOUND)
                 reverseEventLoopGroup == EventLoopFactory.getInstance(EventLoopClient.REVERSE)
              })
   }
}
