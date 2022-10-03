/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.pool

import se.laz.casual.internal.network.NetworkConnection
import se.laz.casual.jca.Address
import se.laz.casual.jca.CasualResourceAdapterException
import se.laz.casual.network.ProtocolVersion
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
         1 * createNetworkConnection(*_) >> Mock(ReferenceCountedNetworkConnection)
      }
      NetworkConnectionPool pool = NetworkConnectionPool.of(poolName, address, poolSize, connectionCreator)
      ProtocolVersion protocolVersion = ProtocolVersion.VERSION_1_0
      when: // working as expected with the correct address
      NetworkConnection connection = pool.getOrCreateConnection(address, protocolVersion, Mock(NetworkListener))
      then:
      noExceptionThrown()
      connection != null
      when: // using the wrong address - throws
      pool.getOrCreateConnection(anotherAddress, protocolVersion, Mock(NetworkListener))
      then:
      thrown(CasualResourceAdapterException)
   }

   def 'size 1 always returns the same instance'()
   {
      int poolSize = 1
      def poolName = 'small-pool'
      Address address = Address.of("nifty", 7771)
      NetworkConnectionCreator connectionCreator = Mock(NetworkConnectionCreator){
         1 * createNetworkConnection(*_) >> Mock(ReferenceCountedNetworkConnection)
      }
      NetworkConnectionPool pool = NetworkConnectionPool.of(poolName, address, poolSize, connectionCreator)
      ProtocolVersion protocolVersion = ProtocolVersion.VERSION_1_0
      when:
      NetworkConnection connection = pool.getOrCreateConnection(address, protocolVersion, Mock(NetworkListener))
      NetworkConnection sameConnection = pool.getOrCreateConnection(address, protocolVersion, Mock(NetworkListener))
      then:
      connection == sameConnection
   }

   def 'big pool, should be able to get other instances'()
   {
      int poolSize = 1000
      def poolName = 'small-pool'
      Address address = Address.of("nifty", 7771)
      NetworkConnectionCreator connectionCreator = Mock(NetworkConnectionCreator){
         1 * createNetworkConnection(*_) >> Mock(ReferenceCountedNetworkConnection)
      }
      NetworkConnectionPool pool = NetworkConnectionPool.of(poolName, address, poolSize, connectionCreator)
      ProtocolVersion protocolVersion = ProtocolVersion.VERSION_1_0
      when:
      NetworkConnection connection = pool.getOrCreateConnection(address, protocolVersion, Mock(NetworkListener))
      NetworkConnection anotherConnection = null
      while(connection == anotherConnection)
      {
         anotherConnection = pool.getOrCreateConnection(address, protocolVersion, Mock(NetworkListener))
      }
      then:
      connection != anotherConnection
   }

}
