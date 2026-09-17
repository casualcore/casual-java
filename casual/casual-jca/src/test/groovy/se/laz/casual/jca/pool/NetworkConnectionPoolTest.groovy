/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.pool

import se.laz.casual.internal.network.NetworkConnection
import se.laz.casual.jca.DomainId
import se.laz.casual.network.outbound.NettyNetworkConnection
import se.laz.casual.jca.Address
import se.laz.casual.jca.CasualResourceAdapterException
import se.laz.casual.network.outbound.NetworkListener
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import spock.lang.Specification

class NetworkConnectionPoolTest extends Specification
{
   def 'using the wrong address'()
   {
      given:
      int poolSize = 5
      def poolName = 'small-pool'
      Address address = Address.of("nifty", 7771)
      Address anotherAddress = Address.of('delta', 8787)
      NetworkConnectionCreator connectionCreator = Mock(NetworkConnectionCreator){
         1 * createNetworkConnection(address,  *_) >> Mock(ReferenceCountedNetworkConnection)
      }
      NetworkConnectionPool pool = NetworkConnectionPool.of(poolName, address, poolSize, connectionCreator)
      when: // working as expected with the correct address
      NetworkConnection connection = pool.getOrCreateConnection(address, Mock(NetworkListener))
      then:
      noExceptionThrown()
      connection != null
      when: // using the wrong address - throws
      pool.getOrCreateConnection(anotherAddress, Mock(NetworkListener))
      then:
      thrown(CasualResourceAdapterException)
   }

   def 'size 1 always returns the same instance'()
   {
      given:
      int poolSize = 1
      def poolName = 'small-pool'
      Address address = Address.of("nifty", 7771)
      NetworkConnectionCreator connectionCreator = Mock(NetworkConnectionCreator){
         1 * createNetworkConnection(address, *_) >> Mock(ReferenceCountedNetworkConnection) {
            tryIncrement() >> true
         }
      }
      NetworkConnectionPool pool = NetworkConnectionPool.of(poolName, address, poolSize, connectionCreator)
      when:
      NetworkConnection connection = pool.getOrCreateConnection(address, Mock(NetworkListener))
      NetworkConnection sameConnection = pool.getOrCreateConnection(address, Mock(NetworkListener))
      then:
      connection == sameConnection
   }

   def 'big pool, should be able to get other instances'()
   {
      given:
      int poolSize = 1000
      def poolName = 'small-pool'
      Address address = Address.of("nifty", 7771)
      NetworkConnectionCreator connectionCreator = Mock(NetworkConnectionCreator){
         1 * createNetworkConnection(address,  *_) >> Mock(ReferenceCountedNetworkConnection)
      }
      NetworkConnectionPool pool = NetworkConnectionPool.of(poolName, address, poolSize, connectionCreator)
      when:
      NetworkConnection connection = pool.getOrCreateConnection(address, Mock(NetworkListener))
      NetworkConnection anotherConnection = null
      while(connection == anotherConnection)
      {
         anotherConnection = pool.getOrCreateConnection(address, Mock(NetworkListener))
      }
      then:
      connection != anotherConnection
   }


   def 'normal outbound reports domain disconnect when any connection is disconnecting: #states'()
   {
      given:
      DomainId domainId = DomainId.of(UUID.randomUUID())
      Address address = Address.of('localhost', 7771)
      NetworkConnectionCreator creator = Mock(NetworkConnectionCreator)
      NetworkConnectionPool pool = NetworkConnectionPool.of('pool', address, 2, creator)
      def references = states.collect { boolean state ->
         ReferenceCountedNetworkConnection.of(Mock(NettyNetworkConnection) {
            isDomainDisconnecting() >> state
            // it is connected to some domain
            getDomainId() >> domainId
         }, pool)
      }
      creator.createNetworkConnection(address, *_) >>> references
      references.each { pool.getOrCreateConnection(address, Mock(NetworkListener)) }

      when:
      boolean disconnecting = pool.isDomainDisconnecting()

      then:
      disconnecting == expected
      references.every { it.@referenceCount == 1 }
      0 * creator.createNetworkConnection(*_)

      where:
      states         | expected
      []             | false
      [false, false] | false
      [false, true]  | true
      [true, false]  | true
   }

   def 'standard pool evicts connection in close path and creates replacement'()
   {
      // note: usually you would run a standard outbound pool on only 1 physical connection
      given:
      def address = Address.of('localhost', 7771)
      def nettyConOne = Mock(NettyNetworkConnection)
      def nettyConTwo = Mock(NettyNetworkConnection)

      def closeStartedLatch = new CountDownLatch(1)
      def closeProceedLatch = new CountDownLatch(1)

      ReferenceCountedNetworkConnection refCountedOne
      ReferenceCountedNetworkConnection refCountedTwo

      // Creator wraps the pool's closeListener to pause before container removal
      def creator = { Address addr, NetworkListener nl, ReferenceCountedNetworkCloseListener poolListener, NetworkListener ol ->
         def delayedListener = { ReferenceCountedNetworkConnection conn ->
            closeStartedLatch.countDown()
            closeProceedLatch.await()
            poolListener.closed(conn)
         } as ReferenceCountedNetworkCloseListener

         // this is so con1 is used on first call and con2 on second
         def physical = (nettyConOne != null) ? nettyConOne : nettyConTwo
         def refCounted = ReferenceCountedNetworkConnection.of(physical, delayedListener)
         if(nettyConOne != null)
         {
            refCountedOne = refCounted
         }
         else
         {
            refCountedTwo = refCounted
         }
         nettyConOne = null
         return refCounted
      }
      def pool = NetworkConnectionPool.of('test-pool', address, 1, creator)

      when: 'initial connection is acquired, then closed in another thread'
      def first = pool.getOrCreateConnection(address, Mock(NetworkListener))
      def closingThread = Thread.start { first.close() }

      // Wait until first has decremented refcount to 0 and set closed = true under referenceLock,
      // but BEFORE pool.closed(first) has executed to remove it from pool.connections
      assert closeStartedLatch.await(5, TimeUnit.SECONDS)

      and: 'a second caller requests a connection while the dying one is still in the container'
      def second = pool.getOrCreateConnection(address, Mock(NetworkListener))

      then:
      !refCountedOne.tryIncrement()
      second != first
      refCountedTwo.tryIncrement()

      cleanup:
      closeProceedLatch.countDown()
      closingThread?.join(5000)
   }
}
