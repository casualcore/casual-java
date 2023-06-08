/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound

import io.netty.channel.Channel
import se.laz.casual.jca.DomainId
import spock.lang.Specification

import java.util.function.Consumer

class DomainDisconnectHandlerTest extends Specification
{
   def 'domain disconnected'()
   {
      given:
      UUID corrid = UUID.randomUUID()
      UUID execution = UUID.randomUUID()
      DomainDisconnectReplyInfo replyInfo = DomainDisconnectReplyInfo.of(corrid, execution)
      Consumer<DomainDisconnectReplyInfo> domainDisconnectReplyFunction = { it ->
         1 * it.accept(replyInfo)
      }
      DomainDisconnectHandler instance = DomainDisconnectHandler.of(Mock(Channel), DomainId.of(UUID.randomUUID()))
      instance.setDomainDisconnectReplyFunction {domainDisconnectReplyFunction}
      when:
      instance.domainDisconnected(replyInfo)
      then:
      noExceptionThrown()
   }
}
