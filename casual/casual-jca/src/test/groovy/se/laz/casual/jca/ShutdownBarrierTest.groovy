/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import spock.lang.Specification

class ShutdownBarrierTest extends Specification
{
   def 'should only continue when predicate is true'()
   {
      given:
      Predicate predicate = Mock(Predicate){
         4 * eval() >>> [true, true, true, false]
      }
      ShutdownBarrier barrier = ShutdownBarrier.of(20, predicate)
      when:
      barrier.intermittentSleep()
      then:
      noExceptionThrown()
   }
   def 'predicate always true, with timeout that should be honoured'()
   {
      given:
      Predicate predicate = Mock(Predicate){
         3 * eval() >>> [true, true, true, true, true, true]
      }
      long timeout = 25
      ShutdownBarrier barrier = ShutdownBarrier.of(20, timeout, predicate)
      when:
      barrier.intermittentSleep()
      then:
      noExceptionThrown()
   }

}
