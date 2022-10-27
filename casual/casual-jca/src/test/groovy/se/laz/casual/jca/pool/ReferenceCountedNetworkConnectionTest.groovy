/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.pool

import se.laz.casual.network.outbound.NettyNetworkConnection
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
         instance.increment()
      }
      when:
      for(int i = 0; i < expectedNumberOfReferences; ++i)
      {
         instance.close()
      }
      then:
      noExceptionThrown()
   }

   def 'concurrent incrementing/decrementing'()
   {
      given:
      def subsequentReferences = 4
      def expectedNumberOfReferences = 5
      def networkConnection = Mock(NettyNetworkConnection)
      def closeListener = Mock(ReferenceCountedNetworkCloseListener)
      // 1st reference count on creation
      ReferenceCountedNetworkConnection instance = ReferenceCountedNetworkConnection.of(networkConnection, closeListener)
      CompletableFuture<Void> incrementFuture = CompletableFuture.runAsync({
         for(int i = 0; i < subsequentReferences; ++i)
         {
            instance.increment()
         }
      })
      CompletableFuture<Void> closeFuture = CompletableFuture.runAsync({
         for(int i = 0; i < expectedNumberOfReferences; ++i)
         {
            instance.close()
         }
      })
      when:
      incrementFuture.join()
      closeFuture.join()
      then:
      instance.increment() == 1
   }

}
