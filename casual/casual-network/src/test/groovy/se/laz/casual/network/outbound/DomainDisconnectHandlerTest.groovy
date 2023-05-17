/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound

import spock.lang.Specification

class DomainDisconnectHandlerTest extends Specification
{
   def 'domain disconnect no transactions in flight'()
   {
      given:
      DomainDisconnectHandler disconnectHandler = DomainDisconnectHandler.of()
      UUID execution = UUID.randomUUID()
      when:
      disconnectHandler.domainDisconnecting(execution)
      then:
      disconnectHandler.hasDomainBeenDisconnected()
      disconnectHandler.getExecution() == execution
      !disconnectHandler.transactionsInfFlight()
   }

   def 'domain disconnect transactions in flight'()
   {
      given:
      TransactionInformation transactionInformation = Mock(TransactionInformation){
         transactionsInFlight() >> true
      }
      DomainDisconnectHandler disconnectHandler = DomainDisconnectHandler.of()
      disconnectHandler.transactionInformation = transactionInformation
      UUID execution = UUID.randomUUID()
      when:
      disconnectHandler.domainDisconnecting(execution)
      then:
      disconnectHandler.hasDomainBeenDisconnected()
      disconnectHandler.getExecution() == execution
      disconnectHandler.transactionsInfFlight()
   }

}
