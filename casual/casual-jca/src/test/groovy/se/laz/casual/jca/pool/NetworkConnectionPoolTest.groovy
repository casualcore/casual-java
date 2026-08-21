/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.pool

import se.laz.casual.internal.network.NetworkConnection
import se.laz.casual.jca.Address
import se.laz.casual.jca.CasualResourceAdapterException
import se.laz.casual.jca.DomainId
import se.laz.casual.network.connection.CasualConnectionException
import se.laz.casual.network.outbound.NettyNetworkConnection
import se.laz.casual.network.outbound.NetworkListener
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

   def 'reverse pool survives managed connection churn, connections only ever close on network error'()
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

}
