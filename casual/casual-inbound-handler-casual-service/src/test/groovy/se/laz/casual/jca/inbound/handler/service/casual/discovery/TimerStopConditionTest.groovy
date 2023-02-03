/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.service.casual.discovery

import se.laz.casual.config.Configuration
import se.laz.casual.config.Inbound
import se.laz.casual.config.Mode
import se.laz.casual.config.Startup
import se.laz.casual.jca.Information
import spock.lang.Specification

class TimerStopConditionTest extends Specification
{
   def 'should stop'()
   {
      expect:
      Information.setInboundStarted(inboundStarted)
      Configuration configuration = Configuration.newBuilder(  )
              .withInbound( Inbound.newBuilder(  )
                      .withStartup( Startup.newBuilder(  )
                              .withMode( mode )
                              .build(  ) )
                      .build(  ) )
              .build(  )
      TimerStopCondition.of().stop( configuration ) == shouldStop
      where:
      mode                           || inboundStarted || shouldStop
      Mode.IMMEDIATE                 || true           || false
      Mode.TRIGGER                   || false          || false
      Mode.TRIGGER                   || true           || true
   }
}
