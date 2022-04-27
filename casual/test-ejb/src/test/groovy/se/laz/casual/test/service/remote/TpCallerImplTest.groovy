/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.test.service.remote

import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.ServiceReturn
import se.laz.casual.api.flags.AtmiFlags
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.Flag
import se.laz.casual.api.flags.ServiceReturnState
import se.laz.casual.connection.caller.CasualCaller
import spock.lang.Specification

class TpCallerImplTest extends Specification
{
   def'tpcall TPFAIL'()
   {
      given:
      def serviceName = "testService"
      def buffer = Mock(CasualBuffer)
      def returnBuffer = Mock(CasualBuffer)
      def serviceReturn = new ServiceReturn<>(returnBuffer, ServiceReturnState.TPFAIL, ErrorState.TPENOENT, 0L)
      def flags = Flag.of(AtmiFlags.NOFLAG)
      def casualCaller = Mock(CasualCaller){
         tpcall(serviceName, buffer, flags) >> {
            serviceReturn
         }
      }
      def instance = new TpCallerImpl(casualCaller)
      when:
      instance.makeTpCall(serviceName, buffer, flags)
      then:
      thrown(ServiceCallFailedException)
   }

   def'tpcall TPSUCCESS'()
   {
      given:
      def serviceName = "testService"
      def buffer = Mock(CasualBuffer)
      def returnBuffer = Mock(CasualBuffer)
      def serviceReturn = new ServiceReturn<>(returnBuffer, ServiceReturnState.TPSUCCESS, ErrorState.OK, 0L)
      def flags = Flag.of(AtmiFlags.NOFLAG)
      def casualCaller = Mock(CasualCaller){
         tpcall(serviceName, buffer, flags) >> {
            serviceReturn
         }
      }
      def instance = new TpCallerImpl(casualCaller)
      when:
      def actual = instance.makeTpCall(serviceName, buffer, flags)
      then:
      actual == returnBuffer
   }

}
