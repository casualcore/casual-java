/*
 * Copyright (c) 2023 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.service.casual.discovery


import se.laz.casual.config.Mode
import se.laz.casual.jca.RuntimeInformation
import spock.lang.Specification

class TimerStopConditionTest extends Specification
{
   def 'should stop, using config file'()
   {
      given:
      RuntimeInformation.setInboundStarted(inboundStarted)

      when:
      TimerStopCondition instance = TimerStopCondition.of( mode )

      then:
      instance.stop( ) == shouldStop

      where:
      mode           || inboundStarted || shouldStop
      Mode.IMMEDIATE || true           || false
      Mode.TRIGGER   || false          || false
      Mode.TRIGGER   || true           || true
   }

}
