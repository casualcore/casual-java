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

}
