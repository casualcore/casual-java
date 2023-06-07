/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound

import se.laz.casual.jca.ConnectionListener
import spock.lang.Specification

import java.util.function.Consumer

class ConnectionHandlerTest extends Specification
{
   def 'domain disconnected'()
   {
      given:
      UUID corrid = UUID.randomUUID()
      UUID execution = UUID.randomUUID()
      DomainDisconnectReplyInfo replyInfo = DomainDisconnectReplyInfo.of(corrid, execution)
      Consumer<DomainDisconnectReplyInfo> dispatch = { info ->
         assert info.getCorrid() == corrid
         assert info.getExecution() == execution
      }
      ConnectionHandler instance = ConnectionHandler.of(dispatch)
      ConnectionListener connectionListener = Mock(ConnectionListener){
         1 * connectionDisabled()
         1 * connectionEnabled()
      }
      instance.addConnectionListener(connectionListener)
      when:
      instance.domainDisconnecting(replyInfo)
      instance.connectionDisabled()
      then:
      noExceptionThrown()
      when:
      instance.connectionEnabled()
      then:
      noExceptionThrown()
   }

   def 'connection disabled but domain has not been disconnected'()
   {
      given:
      UUID corrid = UUID.randomUUID()
      UUID execution = UUID.randomUUID()
      Consumer<DomainDisconnectReplyInfo> dispatch = { info ->
         assert info.getCorrid() == corrid
         assert info.getExecution() == execution
      }
      ConnectionHandler instance = ConnectionHandler.of(dispatch)
      ConnectionListener connectionListener = Mock(ConnectionListener){
         1 * connectionDisabled()
      }
      instance.addConnectionListener(connectionListener)
      when:
      instance.connectionDisabled()
      then:
      thrown(NullPointerException)
   }

}
