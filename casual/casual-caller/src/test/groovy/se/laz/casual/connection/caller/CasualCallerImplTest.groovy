package se.laz.casual.connection.caller

import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.ServiceReturn
import se.laz.casual.api.flags.AtmiFlags
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.Flag
import se.laz.casual.api.flags.ServiceReturnState
import se.laz.casual.api.queue.MessageSelector
import se.laz.casual.api.queue.QueueInfo
import se.laz.casual.api.queue.QueueMessage
import se.laz.casual.jca.CasualConnection
import se.laz.casual.jca.CasualConnectionFactory
import spock.lang.Specification

import javax.resource.ResourceException
import javax.resource.spi.EISSystemException
import javax.transaction.TransactionManager
import java.util.concurrent.CompletableFuture

class CasualCallerImplTest extends Specification
{
    ConnectionFactoryLookup lookup
    CasualCallerImpl instance
    ConnectionFactoryProvider connectionFactoryProvider
    CasualConnectionFactory fallBackConnectionFactory
    ConnectionFactoryEntry fallBackEntry
    TransactionManager transactionManager
    TransactionManagerProvider transactionManagerProvider

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
        fallBackEntry = ConnectionFactoryEntry.of('fallback-jndi', fallBackConnectionFactory)
        lookup = Mock(ConnectionFactoryLookup)
        connectionFactoryProvider = Mock(ConnectionFactoryProvider)
        connectionFactoryProvider.get() >> {
            [fallBackEntry]
        }
        transactionManagerProvider = Mock(TransactionManagerProvider)
        transactionManagerProvider.getTransactionManager() >> { transactionManager }
        instance = new CasualCallerImpl(lookup, connectionFactoryProvider, transactionManagerProvider)
    }

    def 'construction, no entries found - should throw'()
    {
        given:
        ConnectionFactoryProvider provider = Mock(ConnectionFactoryProvider)
        provider.get() >> []
        when:
        new CasualCallerImpl(lookup, provider, Mock(TransactionManagerProvider))
        then:
        thrown(CasualCallerException)
    }

    def 'tpcall fail getting connection from connection factory'()
    {
        given:
        def serviceName = 'echo'
        def connectionFactory = Mock(CasualConnectionFactory)
        connectionFactory.getConnection() >> {
            throw new EISSystemException("oopsie")
        }
        def entries = [ConnectionFactoryEntry.of("someJndiName", connectionFactory)]
        lookup.get(serviceName) >> {
            entries
        }
        when:
        instance.tpcall(serviceName, Mock(CasualBuffer), Flag.of(AtmiFlags.NOFLAG))
        then:
        def e = thrown(CasualResourceException)
        e.getCause() instanceof ResourceException
    }

    def 'tpcall fail, TPENOENT - no such service handled'()
    {
        given:
        def serviceName = 'does not exist'
        def connectionFactory = Mock(CasualConnectionFactory)
        connectionFactory.getConnection() >> {
            throw new EISSystemException("oopsie")
        }
        lookup.get(serviceName) >> {
            []
        }
        when:
        ServiceReturn<CasualBuffer> result = instance.tpcall(serviceName, Mock(CasualBuffer), Flag.of(AtmiFlags.NOFLAG))
        then:
        result.errorState == ErrorState.TPENOENT
        result.serviceReturnState == ServiceReturnState.TPFAIL
    }


    def 'tpacall fail getting connection from connection factory'()
    {
        given:
        def serviceName = 'echo'
        def connectionFactory = Mock(CasualConnectionFactory)
        connectionFactory.getConnection() >> {
            throw new EISSystemException("oopsie")
        }
        def entries = [ConnectionFactoryEntry.of("someJndiName", connectionFactory)]
        lookup.get(serviceName) >> {
            entries
        }
        when:
        instance.tpacall(serviceName, Mock(CasualBuffer), Flag.of(AtmiFlags.NOFLAG))
        then:
        def e = thrown(CasualResourceException)
        e.getCause() instanceof ResourceException
    }


    def 'dequeue fail getting connection from connection factory'()
    {
        given:
        def queueInfo = QueueInfo.createBuilder().withQueueName("bar.foo").build()
        def messageSelector = MessageSelector.of()
        def connectionFactory = Mock(CasualConnectionFactory)
        connectionFactory.getConnection() >> {
            throw new EISSystemException("oopsie")
        }
        def entries = [ConnectionFactoryEntry.of("someJndiName", connectionFactory)]
        lookup.get(queueInfo) >> {
            entries
        }
        when:
        instance.dequeue(queueInfo, messageSelector)
        then:
        def e = thrown(CasualResourceException)
        e.getCause() instanceof ResourceException
    }

    def 'enqueue fail getting connection from connection factory'()
    {
        given:
        def queueInfo = QueueInfo.createBuilder().withQueueName("bar.foo").build()
        def queueMessage = QueueMessage.of(Mock(CasualBuffer))
        def connectionFactory = Mock(CasualConnectionFactory)
        connectionFactory.getConnection() >> {
            throw new EISSystemException("oopsie")
        }
        def entries = [ConnectionFactoryEntry.of("someJndiName", connectionFactory)]
        lookup.get(queueInfo) >> {
            entries
        }
        when:
        instance.enqueue(queueInfo, queueMessage)
        then:
        def e = thrown(CasualResourceException)
        e.getCause() instanceof ResourceException
    }

    def 'tpcall ok'()
    {
        given:
        def serviceName = 'echo'
        def connectionFactory = Mock(CasualConnectionFactory)
        def serviceReturn = createServiceReturn(Mock(CasualBuffer))
        def callingBuffer = Mock(CasualBuffer)
        def flags = Flag.of(AtmiFlags.NOFLAG)
        connectionFactory.getConnection() >> {
            def connection = Mock(CasualConnection)
            1 * connection.tpcall(serviceName, callingBuffer, flags) >> serviceReturn
            return connection
        }
        def entries = [ConnectionFactoryEntry.of("someJndiName", connectionFactory)]
        lookup.get(serviceName) >> {
            entries
        }
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
        def future = new CompletableFuture();
        def callingBuffer = Mock(CasualBuffer)
        def flags = Flag.of(AtmiFlags.NOFLAG)
        connectionFactory.getConnection() >> {
            def connection = Mock(CasualConnection)
            1 * connection.tpacall(serviceName, callingBuffer, flags) >> future
            return connection
        }
        def entries = [ConnectionFactoryEntry.of("someJndiName", connectionFactory)]
        lookup.get(serviceName) >> {
            entries
        }
        when:
        def actual = instance.tpacall(serviceName, callingBuffer, flags)
        then:
        actual == future
    }

    def 'TPNOTRAN tpcall'()
    {
       given:
       def caller = new CasualCallerImpl(lookup, connectionFactoryProvider, transactionManagerProvider)
       caller.tpCaller = Mock(TpCallerFailover)
       1 * transactionManager.suspend()
       1 * transactionManager.resume(_)
       when:
       caller.tpcall("foo", Mock(CasualBuffer), Flag.of(AtmiFlags.TPNOTRAN))
       then:
       noExceptionThrown()
    }

   def 'TPNOTRAN tpacall'()
   {
      given:
      def caller = new CasualCallerImpl(lookup, connectionFactoryProvider, transactionManagerProvider)
      caller.tpCaller = Mock(TpCallerFailover)
      1 * transactionManager.suspend()
      1 * transactionManager.resume(_)
      when:
      caller.tpacall("foo", Mock(CasualBuffer), Flag.of(AtmiFlags.TPNOTRAN))
      then:
      noExceptionThrown()
   }

    def 'enqueue ok'()
    {
        given:
        def queueInfo = QueueInfo.createBuilder().withQueueName("bar.foo").build()
        def queueMessage = QueueMessage.of(Mock(CasualBuffer))
        def connectionFactory = Mock(CasualConnectionFactory)
        def uuid = UUID.randomUUID()
        connectionFactory.getConnection() >> {
            def connection = Mock(CasualConnection)
            1 * connection.enqueue(queueInfo, queueMessage) >> uuid
            return connection
        }
        def entries = [ConnectionFactoryEntry.of("someJndiName", connectionFactory)]
        lookup.get(queueInfo) >> {
            entries
        }
        when:
        def actual = instance.enqueue(queueInfo, queueMessage)
        then:
        actual == uuid
    }

    def 'dequeue ok'()
    {
        given:
        def queueInfo = QueueInfo.createBuilder().withQueueName("bar.foo").build()
        def messageSelector = MessageSelector.of()
        def queueMessage = [QueueMessage.of(Mock(CasualBuffer))]
        def connectionFactory = Mock(CasualConnectionFactory)
        connectionFactory.getConnection() >> {
            def connection = Mock(CasualConnection)
            1 * connection.dequeue(queueInfo, messageSelector) >> queueMessage
            return connection
        }
        def entries = [ConnectionFactoryEntry.of("someJndiName", connectionFactory)]
        lookup.get(queueInfo) >> {
            entries
        }
        when:
        def actual = instance.dequeue(queueInfo, messageSelector)
        then:
        actual == queueMessage
    }

    ServiceReturn<CasualBuffer> createServiceReturn(CasualBuffer casualBuffer)
    {
        new ServiceReturn<CasualBuffer>(casualBuffer, ServiceReturnState.TPSUCCESS, ErrorState.OK, 0)
    }
}
