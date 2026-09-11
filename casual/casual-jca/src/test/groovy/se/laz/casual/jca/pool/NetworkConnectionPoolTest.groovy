/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.pool


import io.netty.channel.embedded.EmbeddedChannel
import jakarta.resource.spi.ConnectionEvent
import jakarta.resource.spi.ConnectionEventListener
import jakarta.resource.spi.ConnectionRequestInfo
import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.flags.AtmiFlags
import se.laz.casual.api.flags.Flag
import se.laz.casual.internal.network.NetworkConnection
import se.laz.casual.jca.Address
import se.laz.casual.jca.CasualConnection
import se.laz.casual.jca.CasualManagedConnection
import se.laz.casual.jca.CasualManagedConnectionFactory
import se.laz.casual.jca.CasualRequestInfo
import se.laz.casual.jca.CasualResourceAdapterException
import se.laz.casual.jca.DomainId
import se.laz.casual.network.connection.CasualConnectionException
import se.laz.casual.network.outbound.ConversationMessageStorageImpl
import se.laz.casual.network.outbound.Correlator
import se.laz.casual.network.outbound.CorrelatorImpl
import se.laz.casual.network.outbound.ErrorInformer
import se.laz.casual.network.outbound.NettyConnectionInformation
import se.laz.casual.network.outbound.NettyNetworkConnection
import se.laz.casual.network.outbound.NetworkListener
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService

class NetworkConnectionPoolTest extends Specification
{
   def cleanup()
   {
      NetworkPoolHandler.getInstance().@pools.clear()
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
      pool.getPoolDomainIds() == (states.isEmpty() ? [] : [domainId])
      0 * creator.createNetworkConnection(*_)

      where:
      states         | expected
      []             | false
      [false, false] | false
      [false, true]  | true
      [true, false]  | true
   }

   def 'reverse domain disconnect queries isolate domains and preserve connection references'()
   {
      given:
      DomainId domainA = DomainId.of(UUID.randomUUID())
      DomainId domainB = DomainId.of(UUID.randomUUID())
      def pool = NetworkConnectionPool.ofReverse('reverse-pool')
      pool.addConnectionForReversePool(Mock(NettyNetworkConnection) {
         getDomainId() >> domainA
         isDomainDisconnecting() >> false
      })
      pool.addConnectionForReversePool(Mock(NettyNetworkConnection) {
         getDomainId() >> domainA
         isDomainDisconnecting() >> true
      })
      pool.addConnectionForReversePool(Mock(NettyNetworkConnection) {
         getDomainId() >> domainB
         isDomainDisconnecting() >> false
      })
      def references = new ArrayList(pool.@connections.@connections)

      when:
      boolean aIsDisconnecting = pool.isDomainDisconnecting(domainA)
      boolean bIsDisconnecting = pool.isDomainDisconnecting(domainB)
      boolean unknownDisconnecting = pool.isDomainDisconnecting(DomainId.of(UUID.randomUUID()))

      then:
      aIsDisconnecting
      !bIsDisconnecting
      !unknownDisconnecting
      pool.getPoolDomainIds() as Set == [domainA, domainB] as Set
      pool.@connections.@connections == references
      references.every { it.@referenceCount == 1 }

      when:
      pool.isDomainDisconnecting()

      then:
      thrown(IllegalStateException)
   }

   def 'domain disconnected connection does not mark its replacement as disconnecting'()
   {
      given:
      DomainId domainId = DomainId.of(UUID.randomUUID())
      NetworkListener closeListener
      def physical = Mock(NettyNetworkConnection) {
         getDomainId() >> domainId
         isDomainDisconnecting() >> true
         addListener(_) >> { NetworkListener listener -> closeListener = listener }
      }
      def pool = NetworkConnectionPool.ofReverse('pool')
      pool.addConnectionForReversePool(physical)
      assert pool.isDomainDisconnecting(domainId)

      when:
      closeListener.disconnected(new IOException('Remote domain closed the connection'))

      then:
      1 * physical.close()
      !pool.isDomainDisconnecting(domainId)
      pool.getPoolDomainIds().isEmpty()

      when:
      pool.addConnectionForReversePool(Mock(NettyNetworkConnection) {
         getDomainId() >> domainId
         isDomainDisconnecting() >> false
      })

      then:
      !pool.isDomainDisconnecting(domainId)
      pool.getPoolDomainIds() == [domainId]
   }

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
         1 * createNetworkConnection(address, *_) >> Mock(ReferenceCountedNetworkConnection){
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

   def 'reverse pool with no connections throws on get'()
   {
      given:
      NetworkConnectionPool pool = NetworkConnectionPool.ofReverse('empty-reverse-pool')
      when:
      pool.getOrCreateConnection(Address.of('asdf', 123), Mock(NetworkListener))
      then:
      thrown(CasualConnectionException)
   }

   def 'adding a connection to a non reverse pool throws'()
   {
      given:
      NetworkConnectionPool pool = NetworkConnectionPool.of('normal-pool', Address.of('asdf', 7771), 1, Mock(NetworkConnectionCreator))
      when:
      pool.addConnectionForReversePool(Mock(NettyNetworkConnection))
      then:
      thrown(CasualResourceAdapterException)
   }

   def 'reverse pool pinned selection'()
   {
      given: 'two connections from instance A and one from instance B'
      DomainId domainA = DomainId.of(UUID.randomUUID())
      DomainId domainB = DomainId.of(UUID.randomUUID())
      def connectionFrom = { DomainId domainId ->
         NettyNetworkConnection connection = Mock(NettyNetworkConnection)
         connection.getDomainId() >> domainId
         return connection
      }
      NetworkConnectionPool pool = NetworkConnectionPool.ofReverse('pinned-reverse-pool')
      pool.addConnectionForReversePool(connectionFrom(domainA))
      pool.addConnectionForReversePool(connectionFrom(domainA))
      pool.addConnectionForReversePool(connectionFrom(domainB))

      when: 'asking for domain A'
      Set<DomainId> seenDomains = [] as Set
      20.times {
         seenDomains << pool.getOrCreateConnection(Address.of('asdf', 123), Mock(NetworkListener), domainA).getDomainId()
      }

      then:
      seenDomains == [domainA] as Set
      and: 'unique domain ids no matter their number of connections'
      pool.getPoolDomainIds() as Set == [domainA, domainB] as Set

      when: 'non connected domain - throws'
      pool.getOrCreateConnection(Address.of('asdf', 123), Mock(NetworkListener), DomainId.of(UUID.randomUUID()))

      then:
      thrown(CasualConnectionException)
   }

   def 'reverse pool retains physical connection across managed connection churn'()
   {
      given: 'a connection from domain A - used by two user applications'
      DomainId domainA = DomainId.of(UUID.randomUUID())
      NettyNetworkConnection physicalConnection = Mock(NettyNetworkConnection) {
         getDomainId() >> domainA
      }
      NetworkConnectionPool pool = NetworkConnectionPool.ofReverse('reverse-pool')
      pool.addConnectionForReversePool(physicalConnection)
      NetworkConnection firstConnection = pool.getOrCreateConnection(Address.of('asdf', 123), Mock(NetworkListener), domainA)
      NetworkConnection secondConnection = pool.getOrCreateConnection(Address.of('asdf', 123), Mock(NetworkListener), domainA)

      expect: 'both connections share the one physical connection'
      firstConnection == secondConnection

      when: 'all users close - the app server churning its managed connections'
      firstConnection.close()
      secondConnection.close()

      then: 'the physical connection remains, the pool holds the initial reference'
      0 * physicalConnection.close()

      and: 'it can be taken again, exactly as a normal outbound pool that is never exhausted'
      pool.getOrCreateConnection(Address.of('whatever', 123), Mock(NetworkListener), domainA) == firstConnection
      pool.getPoolDomainIds() == [domainA]
   }

   def 'unpinned getOrCreateConnection over a reverse pool serves any connected instance - only used for classification, normal usage is always pinned by domain id'()
   {
      given: 'two connections from domain A and one from domain B'
      // Unpinned access is never part of a normal call chain - callers pin by domain id.
      // It exists for the bootstrap: casual-caller has to get *a* connection to ask
      // isReversePool()/getPoolDomainIds() before any domain id is known, and a plain JCA user
      // without casual-caller gets the same "any connected instance" semantics.
      DomainId domainA = DomainId.of(UUID.randomUUID())
      DomainId domainB = DomainId.of(UUID.randomUUID())
      Map<NettyNetworkConnection, NetworkListener> deathListeners = [:]
      def connectionFrom = { DomainId domainId ->
         NettyNetworkConnection connection = Mock(NettyNetworkConnection)
         connection.getDomainId() >> domainId
         // the first registered listener is the pools own removal listener
         connection.addListener(_) >> { NetworkListener listener -> deathListeners.putIfAbsent(connection, listener) }
         return connection
      }
      NettyNetworkConnection connectionA1 = connectionFrom(domainA)
      NettyNetworkConnection connectionA2 = connectionFrom(domainA)
      NettyNetworkConnection connectionB1 = connectionFrom(domainB)
      NetworkConnectionPool pool = NetworkConnectionPool.ofReverse('reverse-pool')
      pool.addConnectionForReversePool(connectionA1)
      pool.addConnectionForReversePool(connectionA2)
      pool.addConnectionForReversePool(connectionB1)

      when: 'drawing many connections unpinned'
      Set<DomainId> seenDomains = [] as Set
      100.times {
         seenDomains << pool.getOrCreateConnection(Address.of('asdf', 123), Mock(NetworkListener)).getDomainId()
      }

      then: 'selection covers the whole pool'
      seenDomains == [domainA, domainB] as Set

      when: 'domain A goes bye bye'
      deathListeners[connectionA1].disconnected(new Exception('instance gone'))
      deathListeners[connectionA2].disconnected(new Exception('instance gone'))

      then: 'its connections are removed and unpinned can only serve domain B'
      pool.getPoolDomainIds() == [domainB]
      pool.getOrCreateConnection(Address.of('asdf', 123), Mock(NetworkListener)).getDomainId() == domainB

      when: 'domain B goes bye bye'
      deathListeners[connectionB1].disconnected(new Exception('instance gone'))
      pool.getOrCreateConnection(Address.of('asdf', 123), Mock(NetworkListener))

      then: 'the pool is empty and allocation fails until an EIS connects again'
      thrown(CasualConnectionException)
      pool.getPoolDomainIds().isEmpty()
   }

   def 'pinned get spread over all connections sharing the same domain id and survives until the last one is gone'()
   {
      given: 'three connections - all with the same domain id'
      DomainId domainId = DomainId.of(UUID.randomUUID())
      Map<NettyNetworkConnection, NetworkListener> deathListeners = [:]
      def connectionFrom = {
         NettyNetworkConnection physicalConnection = Mock(NettyNetworkConnection)
         physicalConnection.getDomainId() >> domainId
         // the first registered listener is the pools own removal listener
         physicalConnection.addListener(_) >> { NetworkListener listener -> deathListeners.putIfAbsent(physicalConnection, listener) }
         return physicalConnection
      }
      NettyNetworkConnection first = connectionFrom()
      NettyNetworkConnection second = connectionFrom()
      NettyNetworkConnection third = connectionFrom()
      NetworkConnectionPool pool = NetworkConnectionPool.ofReverse('reverse-pool')
      [first, second, third].each { pool.addConnectionForReversePool(it) }

      expect: 'one single domain id no matter the number of connections'
      pool.getPoolDomainIds() == [domainId]

      when: 'drawing many pinned connections'
      Set<NetworkConnection> seenConnections = [] as Set
      100.times {
         seenConnections << pool.getOrCreateConnection(Address.of('asdf', 123), Mock(NetworkListener), domainId)
      }

      then: 'every connection is used'
      seenConnections.size() == 3

      when: 'one connection dies'
      deathListeners[first].disconnected(new Exception('connection gone'))
      Set<NetworkConnection> remainingConnections = [] as Set
      100.times {
         remainingConnections << pool.getOrCreateConnection(Address.of('asdf', 123), Mock(NetworkListener), domainId)
      }

      then: 'pinned gets keep working, spread over the survivors, and the domain is still visible'
      remainingConnections.size() == 2
      seenConnections.containsAll(remainingConnections)
      pool.getPoolDomainIds() == [domainId]

      when: 'the last connections die'
      deathListeners[second].disconnected(new Exception('connection gone'))
      deathListeners[third].disconnected(new Exception('connection gone'))
      pool.getOrCreateConnection(Address.of('asdf', 123), Mock(NetworkListener), domainId)

      then: 'the domain is gone and pinned allocation towards it fails'
      thrown(CasualConnectionException)
      pool.getPoolDomainIds().isEmpty()
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
      Thread.start { first.close() }

      // Wait until first has decremented refcount to 0 and set closed = true under referenceLock,
      // but BEFORE pool.closed(first) has executed to remove it from pool.connections
      closeStartedLatch.await()

      and: 'a second caller requests a connection while the dying one is still in the container'
      def second = pool.getOrCreateConnection(address, Mock(NetworkListener))

      then:
      !refCountedOne.tryIncrement()
      second != first
      refCountedTwo.tryIncrement()

      cleanup:
      closeProceedLatch.countDown()
   }

   def 'reverse pool skips closed connection and routes to surviving connection for same domain'()
   {
      given: 'two reverse connections from the same domain'
      def domainA = DomainId.of(UUID.randomUUID())
      def physical1 = createPhysicalConnection(domainA)
      def physical2 = createPhysicalConnection(domainA)

      def pool = NetworkConnectionPool.ofReverse('reverse-pool')
      pool.addConnectionForReversePool(physical1)
      pool.addConnectionForReversePool(physical2)

      // Acquire first connection and close all references so refcount reaches 0
      def conn1 = pool.getOrCreateConnection(Address.of('asdf', 123), Mock(NetworkListener), domainA)
      conn1.close() // ManagedConnection closes (refcount 2 -> 1)
      conn1.close() // Pool closes / network error (refcount 1 -> 0, closed = true)

      when: 'requesting a connection for domain A'
      def allocated = pool.getOrCreateConnection(Address.of('asdf', 123), Mock(NetworkListener), domainA)

      then: 'the closed connection was evicted, returning the healthy connection'
      allocated != conn1
      pool.getPoolDomainIds() == [domainA]
   }

   def 'reverse pool evicts closed connection and throws when no live connections remain'()
   {
      given: 'a single reverse connection that reaches 0 refcount'
      def domainA = DomainId.of(UUID.randomUUID())
      def physical = createPhysicalConnection(domainA)

      def pool = NetworkConnectionPool.ofReverse('reverse-pool')
      pool.addConnectionForReversePool(physical)

      def conn = pool.getOrCreateConnection(Address.of('asdf', 123), Mock(NetworkListener), domainA)
      conn.close() // refcount 2 -> 1
      conn.close() // refcount 1 -> 0

      when: 'requesting a connection for domain A'
      pool.getOrCreateConnection(Address.of('asdf', 123), Mock(NetworkListener), domainA)

      then: 'the closed connection was evicted and CasualConnectionException is thrown'
      thrown(CasualConnectionException)
      pool.getPoolDomainIds().isEmpty()
   }

   def 'reverse pool closes physical connection on network error when no active users'()
   {
      given: 'a reverse connection registered with the pool and no active managed connections'
      DomainId domainA = DomainId.of(UUID.randomUUID())
      NetworkListener networkListener
      NettyNetworkConnection physicalConnection = Mock(NettyNetworkConnection) {
         getDomainId() >> domainA
         addListener(_) >> { NetworkListener l -> networkListener = l }
      }

      NetworkConnectionPool pool = NetworkConnectionPool.ofReverse('reverse-pool')
      pool.addConnectionForReversePool(physicalConnection)

      when: 'an EIS network error occurs while idle in pool'
      networkListener.disconnected(new Exception('EIS network error'))

      then: 'the pool removed it from its domain list and closed the physical connection'
      1 * physicalConnection.close()
      pool.getPoolDomainIds().isEmpty()
   }

   def 'reverse pool closes physical connection after active user releases following network error'()
   {
      given: 'a reverse connection in use by an active application'
      DomainId domainA = DomainId.of(UUID.randomUUID())
      NettyNetworkConnection physicalConnection = Mock(NettyNetworkConnection) {
         getDomainId() >> domainA
      }

      NetworkConnectionPool pool = NetworkConnectionPool.ofReverse('reverse-pool')
      pool.addConnectionForReversePool(physicalConnection)
      NetworkConnection connection = pool.getOrCreateConnection(Address.of('asdf', 123), Mock(NetworkListener), domainA)

      when: 'EIS network error'
      pool.closed(connection)

      then: 'pool evicted the connection, but physical close awaits active managed connection release'
      pool.getPoolDomainIds().isEmpty()

      when: 'active user closes their managed connection ( EIS error, MC is informed, informs appserver, appserver calls destroy on MC -> close on ref counted network connection)'
      connection.close()

      then: 'physical connection is closed'
      1 * physicalConnection.close()
   }

   def 'appserver destroys managed connection and cleans up pool when network connection fails'()
   {
      given: 'a reverse connection pool with a NettyNetworkConnection wired to an EmbeddedChannel and ErrorInformer'
      DomainId domainA = DomainId.of(UUID.randomUUID())
      EmbeddedChannel channel = new EmbeddedChannel()
      Correlator correlator = CorrelatorImpl.of()
      CasualConnectionException networkFailure = new CasualConnectionException('network connection is gone')
      ErrorInformer errorInformer = ErrorInformer.of(networkFailure)
      NettyConnectionInformation ci = NettyConnectionInformation.createBuilder()
         .withAddress(new InetSocketAddress(7771))
         .withDomainId(UUID.randomUUID())
         .withDomainName('test-domain')
         .withCorrelator(correlator)
         .build()
      NettyNetworkConnection physicalConnection = new NettyNetworkConnection(
         ci,
         correlator,
         channel,
         ConversationMessageStorageImpl.of(),
         { Mock(ExecutorService) },
         errorInformer
      )
      // would happen during normal construction
      physicalConnection.setDomainId(domainA)
      channel.closeFuture().addListener({ f ->
         NettyNetworkConnection.handleClose(physicalConnection, errorInformer)
      })

      def poolName = 'simulated-reverse-pool'
      NetworkConnectionPool pool = NetworkPoolHandler.getInstance().getOrCreateReversePool(poolName)
      pool.addConnectionForReversePool(physicalConnection)

      and: 'a managed connection registered with an appserver ConnectionEventListener'
      CasualManagedConnectionFactory mcf = Mock(CasualManagedConnectionFactory) {
         getAddress() >> Mock(Address)
         getNetworkConnectionPoolName() >> poolName
         getNetworkConnectionPoolSize() >> 1
      }
      CasualManagedConnection managedConnection = new CasualManagedConnection(mcf)
      ConnectionRequestInfo requestInfo = CasualRequestInfo.of(domainA)
      CasualConnection handle = (CasualConnection) managedConnection.getConnection(null, requestInfo)

      List<ConnectionEvent> errorEvents = []
      ConnectionEventListener appServerPoolManager = Mock(ConnectionEventListener) {
         connectionErrorOccurred(_) >> { ConnectionEvent event ->
            errorEvents << event
            // Simulate the application server reaction: destroy the failed managed connection
            managedConnection.destroy()
         }
      }
      managedConnection.addConnectionEventListener(appServerPoolManager)

      when: 'the physical network connection fails and NettyNetworkConnection::handleClose is invoked'
      channel.disconnect()

      then: 'the appserver connection event listener was notified with CONNECTION_ERROR_OCCURRED'
      errorEvents.size() == 1
      errorEvents[0].id == ConnectionEvent.CONNECTION_ERROR_OCCURRED
      errorEvents[0].exception == networkFailure

      and: 'the failed connection is evicted from the pool'
      pool.getPoolDomainIds().isEmpty()

      and: 'the physical connection channel is closed'
      !physicalConnection.isActive()

      when: 'an application attempts to use the connection handle after the appserver destroyed the managed connection'
      handle.tpcall('myService', Mock(CasualBuffer), Flag.of(AtmiFlags.NOFLAG))

      then: 'the call fails because the connection is closed/destroyed'
      thrown(CasualConnectionException)
   }

   private NettyNetworkConnection createPhysicalConnection(DomainId domainId)
   {
      NettyNetworkConnection connection = Mock(NettyNetworkConnection)
      connection.getDomainId() >> domainId
      return connection
   }

}
