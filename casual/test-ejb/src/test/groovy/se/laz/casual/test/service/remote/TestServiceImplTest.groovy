/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.test.service.remote

import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.flags.AtmiFlags
import se.laz.casual.api.flags.Flag
import se.laz.casual.jca.inbound.handler.InboundRequest
import se.laz.casual.jca.inbound.handler.InboundResponse

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable
import spock.lang.Specification

class TestServiceImplTest extends Specification
{
   def 'no JAVA_FORWARD_SERVICE_NAME, throws'()
   {
      given:
      TestServiceImpl instance = new TestServiceImpl(Mock(TpCaller))
      when:
      instance.forward(Mock(InboundRequest))
      then:
      thrown(ForwardDefinitionMissingException)
   }

   def 'JAVA_FORWARD_SERVICE_NAME defined, forwards call to that service'()
   {
      given:
      def javaForwardServiceName = 'forwardService'
      def buffer = Mock(CasualBuffer)
      def returnBuffer = Mock(CasualBuffer)
      def inboundRequest = Mock(InboundRequest){
         1 * getBuffer() >> {
            buffer
         }
      }
      def flags = Flag.of(AtmiFlags.NOFLAG)
      def tpCaller = Mock(TpCaller){
         1 * makeTpCall(javaForwardServiceName, buffer, flags) >> {
            returnBuffer
         }
      }
      def instance = new TestServiceImpl(tpCaller)
      when:
      InboundResponse actual
      withEnvironmentVariable(TestServiceImpl.JAVA_FORWARD_ENV_NAME, javaForwardServiceName).execute( {
         actual = instance.forward(inboundRequest)
      } )
      then:
      actual.getBuffer() == returnBuffer
   }

}
