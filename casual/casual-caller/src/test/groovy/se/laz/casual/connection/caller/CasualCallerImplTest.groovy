/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller

import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.ServiceReturn
import se.laz.casual.api.flags.AtmiFlags
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.Flag
import se.laz.casual.api.flags.ServiceReturnState
import se.laz.casual.api.queue.*
import se.laz.casual.connection.caller.entities.CacheEntry
import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry
import se.laz.casual.connection.caller.entities.ConnectionFactoryProducer
import se.laz.casual.connection.caller.pool.PoolManager
import se.laz.casual.connection.caller.pool.PoolMatcher
import se.laz.casual.jca.CasualConnection
import se.laz.casual.jca.CasualConnectionFactory
import se.laz.casual.jca.CasualRequestInfo
import se.laz.casual.jca.DomainId
import spock.lang.Specification

import javax.transaction.Transaction
import javax.transaction.TransactionManager
import java.util.concurrent.CompletableFuture

class CasualCallerImplTest extends Specification
{
    CasualCallerImpl instance
    ConnectionFactoryEntryStore connectionFactoryProvider
    CasualConnectionFactory fallBackConnectionFactory
    ConnectionFactoryEntry fallBackEntry
    TransactionManager transactionManager
    TransactionManagerProvider transactionManagerProvider
    TransactionLess transactionLess

    def setup()
    {
        transactionManager = Mock(TransactionManager)
        fallBackConnectionFactory = Mock(CasualConnectionFactory)
        def fallbackConnection = Mock(CasualConnection)
        fallbackConnection.tpcall('does not exist', _, _) >> {
            ServiceReturn<CasualBuffer> serviceReturn = new ServiceReturn<CasualBuffer>(Mock(CasualBuffer), ServiceReturnState.TPFAIL, ErrorState.TPENOENT, 0)
            return serviceReturn
        }
        fallBackConnectionFactory.getConnection() >> fallbackConnection
        def fallbackProducer = Mock(ConnectionFactoryProducer) {
           getConnectionFactory() >> {
              fallbackConnection
           }
           getJndiName() >> {
              'fallback-jndi'
           }
        }
        fallBackEntry = ConnectionFactoryEntry.of(fallbackProducer)
        connectionFactoryProvider = Mock(ConnectionFactoryEntryStore)
        connectionFactoryProvider.get() >> {
            [fallBackEntry]
        }
        transactionManagerProvider = Mock(TransactionManagerProvider)
        transactionManagerProvider.getTransactionManager() >> { transactionManager }
        transactionLess = new TransactionLess(transactionManagerProvider)
        instance = new CasualCallerImpl(connectionFactoryProvider, transactionLess, Mock(TpCaller), Mock(QueueCaller))
    }

    def 'construction, no entries found - should throw'()
    {
        given:
        ConnectionFactoryEntryStore provider = Mock(ConnectionFactoryEntryStore)
        provider.get() >> []
        when:
        new CasualCallerImpl(provider, Mock(TransactionLess), Mock(TpCaller), Mock(QueueCaller))
        then:
        thrown(CasualCallerException)
    }


    def 'tpcall fail, TPENOENT - no such service handled'()
    {
        given:
        def serviceName = 'does not exist'
        def poolManager = Stub(PoolManager) {
           getPools() >> {
              []
           }
        }
        def poolMatcher = Stub(PoolMatcher) {
           match(_, _) >> {
              return []
           }
        }
        def cache = Stub(Cache){
           get(_) >> {
              []
           }
        }
        TpCaller tpCaller = new TpCallerImpl(poolMatcher, cache, poolManager)
        def instance = new CasualCallerImpl(connectionFactoryProvider, transactionLess, tpCaller, Mock(QueueCaller))
        when:
        ServiceReturn<CasualBuffer> result = instance.tpcall(serviceName, Mock(CasualBuffer), Flag.of(AtmiFlags.NOFLAG))
        then:
        result.errorState == ErrorState.TPENOENT
        result.serviceReturnState == ServiceReturnState.TPFAIL
    }

    def 'tpcall ok'()
    {
        given:
        def serviceName = 'echo'
        def connectionFactory = Mock(CasualConnectionFactory)
        def serviceReturn = createServiceReturn(Mock(CasualBuffer))
        def callingBuffer = Mock(CasualBuffer)
        def flags = Flag.of(AtmiFlags.NOFLAG)
        def domainId = DomainId.of(UUID.randomUUID())
        connectionFactory.getConnection(_) >> { arguments ->
           CasualRequestInfo requestInfo = arguments[0]
           assert requestInfo.getDomainId().get() == domainId
           def connection = Mock(CasualConnection)
           1 * connection.tpcall(serviceName, callingBuffer, flags) >> serviceReturn
           return connection
        }

        def poolMatcher = Stub(PoolMatcher) {
           match(_, _) >> {
              return []
           }
        }
        def producer = Stub(ConnectionFactoryProducer){
           getConnectionFactory() >> {
              connectionFactory
           }
           getJndiName() >> {
              'someJndiName'
           }
        }
        CacheEntry cacheEntry = CacheEntry.of(domainId, ConnectionFactoryEntry.of(producer))
        def cache = Stub(Cache){
           get(_) >> {
              [cacheEntry]
           }
        }
        TpCaller tpCaller = new TpCallerImpl(poolMatcher, cache, Mock(PoolManager))
        def instance = new CasualCallerImpl(connectionFactoryProvider, transactionLess, tpCaller, Mock(QueueCaller))
        when:
        def actual = instance.tpcall(serviceName, callingBuffer, flags)
        then:
        actual == serviceReturn
    }

   def 'tpacall ok'()
   {
      given:
      def serviceName = 'echo'
      def connectionFactory = Mock(CasualConnectionFactory)
      def serviceReturn = new CompletableFuture()
      def callingBuffer = Mock(CasualBuffer)
      def flags = Flag.of(AtmiFlags.NOFLAG)
      def domainId = DomainId.of(UUID.randomUUID())
      connectionFactory.getConnection(_) >> { arguments ->
         CasualRequestInfo requestInfo = arguments[0]
         assert requestInfo.getDomainId().get() == domainId
         def connection = Mock(CasualConnection)
         1 * connection.tpacall(serviceName, callingBuffer, flags) >> serviceReturn
         return connection
      }
      def poolMatcher = Stub(PoolMatcher) {
         match(_, _) >> {
            return []
         }
      }
      def producer = Stub(ConnectionFactoryProducer){
         getConnectionFactory() >> {
            connectionFactory
         }
         getJndiName() >> {
            'someJndiName'
         }
      }
      CacheEntry cacheEntry = CacheEntry.of(domainId, ConnectionFactoryEntry.of(producer))
      def cache = Stub(Cache){
         get(_) >> {
            [cacheEntry]
         }
      }
      TpCaller tpCaller = new TpCallerImpl(poolMatcher, cache, Mock(PoolManager))
      def instance = new CasualCallerImpl(connectionFactoryProvider, transactionLess, tpCaller, Mock(QueueCaller))
      when:
      def actual = instance.tpacall(serviceName, callingBuffer, flags)
      then:
      actual == serviceReturn
   }

    def 'TPNOTRAN tpcall, in transaction'()
    {
       given:
       1 * transactionManager.suspend() >> {
          Mock(Transaction)
       }
       def caller = new CasualCallerImpl(connectionFactoryProvider, new TransactionLess(transactionManagerProvider), Mock(TpCaller), Mock(QueueCaller))
       caller.tpCaller = Mock(TpCallerImpl)
       1 * transactionManager.resume(_)
       when:
       caller.tpcall("foo", Mock(CasualBuffer), Flag.of(AtmiFlags.TPNOTRAN))
       then:
       noExceptionThrown()
    }

   def 'TPNOTRAN tpcall, no transaction'()
   {
      given:
      1 * transactionManager.suspend() >> {
         null
      }
      def caller = new CasualCallerImpl(connectionFactoryProvider, new TransactionLess(transactionManagerProvider), Mock(TpCaller), Mock(QueueCaller))
      caller.tpCaller = Mock(TpCallerImpl)
      0 * transactionManager.resume(_)
      when:
      caller.tpcall("foo", Mock(CasualBuffer), Flag.of(AtmiFlags.TPNOTRAN))
      then:
      noExceptionThrown()
   }


   def 'TPNOTRAN tpacall in transaction'()
   {
      given:
      1 * transactionManager.suspend() >> {
         Mock(Transaction)
      }
      def caller = new CasualCallerImpl(connectionFactoryProvider, new TransactionLess(transactionManagerProvider), Mock(TpCaller), Mock(QueueCaller))
      caller.tpCaller = Mock(TpCallerImpl)
      1 * transactionManager.resume(_)
      when:
      caller.tpacall("foo", Mock(CasualBuffer), Flag.of(AtmiFlags.TPNOTRAN))
      then:
      noExceptionThrown()
   }

   def 'TPNOTRAN tpacall no transaction'()
   {
      given:
      1 * transactionManager.suspend() >> {
         null
      }
      def caller = new CasualCallerImpl(connectionFactoryProvider, new TransactionLess(transactionManagerProvider), Mock(TpCaller), Mock(QueueCaller))
      caller.tpCaller = Mock(TpCallerImpl)
      0 * transactionManager.resume(_)
      when:
      caller.tpacall("foo", Mock(CasualBuffer), Flag.of(AtmiFlags.TPNOTRAN))
      then:
      noExceptionThrown()
   }

   def 'enqueue ok'()
    {
        given:
        def queueInfo = QueueInfo.of("bar.foo")
        def queueMessage = QueueMessage.of(Mock(CasualBuffer))
        def connectionFactory = Mock(CasualConnectionFactory)
        def uuid = UUID.randomUUID()
        def domainId = DomainId.of(UUID.randomUUID())
        connectionFactory.getConnection(_) >> { arguments ->
           CasualRequestInfo requestInfo = arguments[0]
           assert requestInfo.getDomainId().get() == domainId
           def connection = Mock(CasualConnection)
           1 * connection.enqueue(queueInfo, queueMessage) >> EnqueueReturn.createBuilder().withErrorState(ErrorState.OK).withId(uuid).build()
           return connection
        }
        def poolMatcher = Stub(PoolMatcher) {
           match(_, _) >> {
              return []
           }
        }
        def producer = Mock(ConnectionFactoryProducer){
           getConnectionFactory() >> {
              connectionFactory
           }
           getJndiName() >> {
              'someJndiName'
           }
        }
        CacheEntry cacheEntry = CacheEntry.of(domainId, ConnectionFactoryEntry.of(producer))
        def cache = Stub(Cache){
           get(_) >> {
              [cacheEntry]
           }
        }
        QueueCaller queueCaller = new QueueCallerImpl(poolMatcher, cache, Mock(PoolManager))
        def instance = new CasualCallerImpl(connectionFactoryProvider, transactionLess, Mock(TpCaller), queueCaller)
        when:
        EnqueueReturn actual = instance.enqueue(queueInfo, queueMessage)
        then:
        actual == EnqueueReturn.createBuilder().withErrorState(ErrorState.OK).withId(uuid).build()
    }

   def 'dequeue ok'()
   {
      given:
      def queueInfo = QueueInfo.of("bar.foo")
      def queueMessage = QueueMessage.of(Mock(CasualBuffer))
      def messageSelector = MessageSelector.of()
      def connectionFactory = Mock(CasualConnectionFactory)
      def uuid = UUID.randomUUID()
      def domainId = DomainId.of(UUID.randomUUID())
      connectionFactory.getConnection(_) >> { arguments ->
         CasualRequestInfo requestInfo = arguments[0]
         assert requestInfo.getDomainId().get() == domainId
         def connection = Mock(CasualConnection)
         1 * connection.dequeue(queueInfo, messageSelector) >> DequeueReturn.createBuilder().withErrorState(ErrorState.OK).withQueueMessage(queueMessage).build()
         return connection
      }
      def poolMatcher = Stub(PoolMatcher) {
         match(_, _) >> {
            return []
         }
      }
      def producer = Mock(ConnectionFactoryProducer){
         getConnectionFactory() >> {
            connectionFactory
         }
         getJndiName() >> {
            'someJndiName'
         }
      }
      CacheEntry cacheEntry = CacheEntry.of(domainId, ConnectionFactoryEntry.of(producer))
      def cache = Stub(Cache){
         get(_) >> {
            [cacheEntry]
         }
      }
      QueueCaller queueCaller = new QueueCallerImpl(poolMatcher, cache, Mock(PoolManager))
      def instance = new CasualCallerImpl(connectionFactoryProvider, transactionLess, Mock(TpCaller), queueCaller)
      when:
      def actual = instance.dequeue(queueInfo, messageSelector)
      then:
      actual == DequeueReturn.createBuilder().withErrorState(ErrorState.OK).withQueueMessage(queueMessage).build()
   }

   ServiceReturn<CasualBuffer> createServiceReturn(CasualBuffer casualBuffer)
   {
      new ServiceReturn<CasualBuffer>(casualBuffer, ServiceReturnState.TPSUCCESS, ErrorState.OK, 0)
   }
}
