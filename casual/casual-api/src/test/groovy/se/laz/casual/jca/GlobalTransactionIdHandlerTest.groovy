/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca

import spock.lang.Specification

class GlobalTransactionIdHandlerTest  extends Specification
{
   def 'normal flow'()
   {
      given:
      GlobalTransactionIdHandler instance = GlobalTransactionIdHandler.of()
      DomainId idConnectionOne = DomainId.of()
      DomainId idConnectionTwo = DomainId.of()
      GlobalTransactionId knownId = GlobalTransactionId.of('asdf' as byte[])
      def globalTransactionIdsForConnectionOne = [knownId,
                                                  GlobalTransactionId.of('qwerty' as byte[])] as Set<GlobalTransactionId>
      def globalTransactionIdsForConnectionTwo = [GlobalTransactionId.of('Birch' as byte[]),
                                                  GlobalTransactionId.of('Oak' as byte[])] as Set<GlobalTransactionId>
      when:
      globalTransactionIdsForConnectionOne.each{id -> instance.addGtrid(id, idConnectionOne)}
      then:
      instance.exists(knownId)
      instance.getPreparedGtrids() == globalTransactionIdsForConnectionOne
      instance.getDomainIdToPreparedGtrids().get(idConnectionOne) == globalTransactionIdsForConnectionOne
      instance.getDomainIdToPreparedGtrids().get(idConnectionTwo) == null
      globalTransactionIdsForConnectionOne.each {id -> assert instance.exists(id)}
      when:
      globalTransactionIdsForConnectionTwo.each{id -> instance.addGtrid(id, idConnectionTwo)}
      then:
      instance.exists(knownId)
      instance.getPreparedGtrids() == globalTransactionIdsForConnectionOne + globalTransactionIdsForConnectionTwo
      instance.getDomainIdToPreparedGtrids().get(idConnectionOne) == globalTransactionIdsForConnectionOne
      instance.getDomainIdToPreparedGtrids().get(idConnectionTwo) == globalTransactionIdsForConnectionTwo
      (globalTransactionIdsForConnectionOne + globalTransactionIdsForConnectionTwo).each {id -> assert instance.exists(id)}
      when:
      instance.removeGtrid(knownId, idConnectionOne)
      // idempotent
      instance.removeGtrid(knownId, idConnectionOne)
      then:
      !instance.exists(knownId)
      instance.getPreparedGtrids() == (globalTransactionIdsForConnectionOne - knownId) + globalTransactionIdsForConnectionTwo
      instance.getDomainIdToPreparedGtrids().get(idConnectionOne) == globalTransactionIdsForConnectionOne - knownId
      instance.getDomainIdToPreparedGtrids().get(idConnectionTwo) == globalTransactionIdsForConnectionTwo
      ((globalTransactionIdsForConnectionOne - knownId) + globalTransactionIdsForConnectionTwo).each {id -> assert instance.exists(id)}
      when:
      instance.removeAllGtridsFor(idConnectionTwo)
      then:
      instance.getPreparedGtrids() == (globalTransactionIdsForConnectionOne - knownId)
      instance.getDomainIdToPreparedGtrids().get(idConnectionOne) == globalTransactionIdsForConnectionOne - knownId
      instance.getDomainIdToPreparedGtrids().get(idConnectionTwo) == null
      (globalTransactionIdsForConnectionOne - knownId).each {id -> assert instance.exists(id)}
   }
}
