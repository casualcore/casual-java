/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.buffer

import spock.lang.Specification
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class InboundRequestInfoTest extends Specification
{
   def 'creation'()
   {
      given:
      def methodName = 'toString'
      Method proxyMethod = String.class.getMethod(methodName)
      Method realMethod = String.class.getMethod(methodName)
      String serviceName = 'shiny lemon'
      Proxy proxy = Mock(Proxy)
      when:
      InboundRequestInfo requestInfo = InboundRequestInfo.createBuilder()
              .withProxy(proxy)
              .withProxyMethod(proxyMethod)
              .withRealMethod(realMethod)
              .withServiceName(serviceName)
              .build()
      then:
      requestInfo.getProxy() == proxy
      requestInfo.getProxyMethod().get() == proxyMethod
      requestInfo.getRealMethod().get() == realMethod
      requestInfo.getServiceName().get() == serviceName
   }

   def 'failed creation - mandatory missing '()
   {
      given:
      def methodName = 'toString'
      Method proxyMethod = String.class.getMethod(methodName)
      Method realMethod = String.class.getMethod(methodName)
      String serviceName = 'shiny lemon'
      when:
      InboundRequestInfo requestInfo = InboundRequestInfo.createBuilder()
              .withProxyMethod(proxyMethod)
              .withRealMethod(realMethod)
              .withServiceName(serviceName)
              .build()
      then:
      thrown(NullPointerException)
   }

}
