/*
 * Copyright (c) 2022 + 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.pool

import se.laz.casual.network.outbound.NettyNetworkConnection
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class ReferenceCountedNetworkConnectionTest extends Specification
{
   def 'test only 1 reference count ( initial )'()
   {
      given:
      def networkConnection = Mock(NettyNetworkConnection) {
         1 * close()
      }
      def closeListener = Mock(ReferenceCountedNetworkCloseListener){
         1 * closed(_)
      }
      // 1st reference count on creation
      ReferenceCountedNetworkConnection instance = ReferenceCountedNetworkConnection.of(networkConnection, closeListener)
      when:
      instance.close()
      then:
      noExceptionThrown()
   }

   def 'test reference counting and effects when the last reference is closed - more than initial reference'()
   {
      given:
      def subsequentReferences = 4
      def expectedNumberOfReferences = 5
      def networkConnection = Mock(NettyNetworkConnection) {
         1 * close()
      }
      def closeListener = Mock(ReferenceCountedNetworkCloseListener){
         1 * closed(_)
      }
      // 1st reference count on creation
      ReferenceCountedNetworkConnection instance = ReferenceCountedNetworkConnection.of(networkConnection, closeListener)
      for(int i = 0; i < subsequentReferences; ++i)
      {
         assert instance.tryIncrement()
      }
      when:
      for(int i = 0; i < expectedNumberOfReferences; ++i)
      {
         instance.close()
      }
      then:
      noExceptionThrown()
   }

   def 'closed connection can not be taken again'()
   {
      given:
      def networkConnection = Mock(NettyNetworkConnection)
      def closeListener = Mock(ReferenceCountedNetworkCloseListener)
      ReferenceCountedNetworkConnection instance = ReferenceCountedNetworkConnection.of(networkConnection, closeListener)

      when: 'a second user takes and both release'
      instance.tryIncrement()
      instance.close()
      instance.close()

      then: 'the physical connection is closed, once, and the listener told'
      1 * networkConnection.close()
      1 * closeListener.closed(instance)

      expect: 'a closed connection can not be taken again'
      !instance.tryIncrement()
   }

   def 'concurrent incrementing/decrementing closes the physical connection exactly once'()
   {
      given:
      def subsequentReferences = 4
      def totalReferences = 5
      def networkConnection = Mock(NettyNetworkConnection){
         1 * close()
      }
      def closeListener = Mock(ReferenceCountedNetworkCloseListener){
         1 * closed(_)
      }
      // 1st reference count on creation
      ReferenceCountedNetworkConnection instance = ReferenceCountedNetworkConnection.of(networkConnection, closeListener)
      when:
      CompletableFuture<Void> incrementFuture = CompletableFuture.runAsync({
         for(int i = 0; i < subsequentReferences; ++i)
         {
            instance.tryIncrement()
         }
      })
      CompletableFuture<Void> closeFuture = CompletableFuture.runAsync({
         for(int i = 0; i < totalReferences; ++i)
         {
            instance.close()
         }
      })
      incrementFuture.join()
      closeFuture.join()
      then: 'no matter the interleaving, exactly one physical close and no resurrection'
      !instance.tryIncrement()
   }

}
