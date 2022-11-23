/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller

import se.laz.casual.api.Conversation
import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.ServiceReturn
import se.laz.casual.api.conversation.TpConnectReturn
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.Flag
import se.laz.casual.connection.caller.conversation.ConversationImpl
import se.laz.casual.jca.CasualConnection
import se.laz.casual.jca.CasualConnectionFactory
import spock.lang.Shared
import spock.lang.Specification


class TpCallerFailoverTest extends Specification
{
    @Shared
    def serviceName = 'some totally real service'
    @Shared
    def priorityHigh = 3L
    @Shared
    def priorityLow = 31L
    @Shared
    def conFacHighJndi = 'conFacHighJndi'
    @Shared
    def conFacLowJndi = 'conFacLowJndi'
    @Shared
    CasualConnectionFactory conFacHigh
    @Shared
    CasualConnectionFactory conFacLow
    @Shared
    CasualConnection conHigh
    @Shared
    CasualConnection conLow
    @Shared
    ConnectionFactoryProducer connectionFactoryProducerHigh
    @Shared
    ConnectionFactoryProducer connectionFactoryProducerLow

    TpCallerFailover tpCaller
    ConnectionFactoryEntryStore connectionFactoryProvider
    Cache cache
    Lookup lookup

    def data
    def flags
    ConnectionFactoryLookupService lookupService

    def setup()
    {
        conHigh = Mock(CasualConnection)
        conLow = Mock(CasualConnection)
        conFacHigh = Mock(CasualConnectionFactory)
        conFacLow = Mock(CasualConnectionFactory)
        conFacHigh.getConnection() >> conHigh
        conFacLow.getConnection() >> conLow

        tpCaller = new TpCallerFailover()

        data = Mock(CasualBuffer)
        flags = Mock(Flag)

        connectionFactoryProvider = Mock(ConnectionFactoryEntryStore)
        cache =  new Cache()
        lookup = Mock(Lookup)
        lookupService = new ConnectionFactoryLookupService()
        lookupService.connectionFactoryProvider = connectionFactoryProvider
        lookupService.cache = cache
        lookupService.lookup = lookup

       connectionFactoryProducerHigh = Mock(ConnectionFactoryProducer)
       connectionFactoryProducerHigh.getConnectionFactory() >> {
          conFacHigh
       }
       connectionFactoryProducerHigh.getJndiName() >> {
          conFacHighJndi
       }

       connectionFactoryProducerLow = Mock(ConnectionFactoryProducer)
       connectionFactoryProducerLow.getConnectionFactory() >> {
          conFacLow
       }
       connectionFactoryProducerLow.getJndiName() >> {
          conFacLowJndi
       }


        connectionFactoryProvider.get() >> [
                ConnectionFactoryEntry.of(connectionFactoryProducerHigh),
                ConnectionFactoryEntry.of(connectionFactoryProducerLow)
        ]
    }

    def "2 connection factories - first connection throws exception, second succeeds"()
    {
        setup:
        lookup.find(serviceName, connectionFactoryProvider.get()) >> ConnectionFactoriesByPriority.of([
                (priorityHigh): [ConnectionFactoryEntry.of(connectionFactoryProducerHigh)],
                (priorityLow): [ConnectionFactoryEntry.of(connectionFactoryProducerLow)]
        ])
        def failMessage = 'Connection is fail'

        def someServiceReturn = new ServiceReturn(null, null, null, 0)
        def realConversation = Mock(Conversation)
        def someTpConnectReturn = TpConnectReturn.of(realConversation)

        when:
        def result = tpCaller.tpcall(serviceName, data, flags, lookupService)
        then:
        1 * conFacHigh.getConnection() >> {throw new javax.resource.ResourceException(failMessage)}
        0 * conHigh.tpcall(serviceName, data, flags) >> {throw new RuntimeException("This should not happen because getConnection should fail")}
        1 * conLow.tpcall(serviceName, data, flags) >> someServiceReturn
        result == someServiceReturn
        when:
        TpConnectReturn tpConnectReturn = tpCaller.tpconnect(serviceName, data, flags, lookupService)
        Conversation conversation = tpConnectReturn.getConversation().orElseThrow({"missing conversation!"})
        ConversationImpl conversationImpl
        if(conversation instanceof ConversationImpl)
        {
           conversationImpl = (ConversationImpl) conversation
        }
        then:
        0 * conHigh.tpconnect(serviceName, data, flags) >> {throw new RuntimeException("This should not happen because getConnection should fail")}
        1 * conLow.tpconnect(serviceName, data, flags) >> someTpConnectReturn
        tpConnectReturn.getErrorState() == someTpConnectReturn.getErrorState()
        conversation instanceof ConversationImpl
        conversationImpl.conversation == realConversation
    }

    def "2 connection factories with same priority - both are called and fail, exception is thrown"()
    {
        setup:
        lookup.find(serviceName, connectionFactoryProvider.get()) >> ConnectionFactoriesByPriority.of([
                (priorityHigh): [ConnectionFactoryEntry.of(connectionFactoryProducerHigh), ConnectionFactoryEntry.of(connectionFactoryProducerLow)]
        ])
        def failMessage = 'Connection is fail'
        when:
        def result = tpCaller.tpcall(serviceName, data, flags, lookupService)

        then:
        1 * conFacHigh.getConnection() >> {throw new javax.resource.ResourceException(failMessage)}
        1 * conFacLow.getConnection() >> {throw new javax.resource.ResourceException(failMessage)}
        def e = thrown(CasualResourceException)
        e.message == 'Call failed to all 2 available casual connections.'
        result == null
    }

    def "lots of connection factory entries - all 64 of them fail, exception is thrown"()
    {
        setup:
        def priorities = 8
        def entriesPerPriority = 8
        Map<Long, List<ConnectionFactoryEntry>> cacheMap = [:]
        for (long prioIndex = 1; prioIndex <= priorities; prioIndex++)
        {
            List<ConnectionFactoryEntry> listOfEntries = []
            for (int i = 0; i < entriesPerPriority; i++)
            {
                ConnectionFactoryProducer producer = Mock(ConnectionFactoryProducer)
                producer.getConnectionFactory() >> {
                   conFacHigh
                }
                producer.getJndiName() >> {
                   conFacHighJndi+prioIndex+":"+i
                }
                listOfEntries.add(ConnectionFactoryEntry.of(producer))
            }
            cacheMap.put(prioIndex, listOfEntries)
        }

        lookup.find(serviceName, connectionFactoryProvider.get()) >> ConnectionFactoriesByPriority.of(cacheMap)
        def failMessage = 'Connection is fail'

        when:
        def result = tpCaller.tpcall(serviceName, data, flags, lookupService)

        then:
        (priorities*entriesPerPriority) * conFacHigh.getConnection() >> {throw new javax.resource.ResourceException(failMessage)}
        def e = thrown(CasualResourceException)
        e.message == 'Call failed to all 64 available casual connections.'
        result == null
    }



    def "lots of connection factory entries - 64 of them fail but final set of entries work, ends in success"()
    {
        setup:
        def priorities = 8
        def entriesPerPriority = 8
        Map<Long, List<ConnectionFactoryEntry>> cacheMap = [:]
        // Will fail
        for (long prioIndex = 1; prioIndex <= priorities; prioIndex++)
        {
            List<ConnectionFactoryEntry> listOfEntries = []
            for (int i = 0; i < entriesPerPriority; i++)
            {
               ConnectionFactoryProducer producer = Mock(ConnectionFactoryProducer)
               producer.getConnectionFactory() >> {
                  conFacHigh
               }
               producer.getJndiName() >> {
                  conFacHighJndi+prioIndex+":"+i
               }
               listOfEntries.add(ConnectionFactoryEntry.of(producer))
            }
            cacheMap.put(prioIndex, listOfEntries)
        }

        // Will succeed. Set of (entiresPerPriority) that will be called last, any should return OK.
        List<ConnectionFactoryEntry> listOfEntries = []
        for (int i = 0; i < entriesPerPriority; i++)
        {
           ConnectionFactoryProducer producer = Mock(ConnectionFactoryProducer)
           producer.getConnectionFactory() >> {
              conFacLow
           }
           producer.getJndiName() >> {
              conFacHighJndi+(priorities+1L)+":"+i
           }
            listOfEntries.add(ConnectionFactoryEntry.of(producer))
        }
        cacheMap.put(priorities+1L, listOfEntries)

        lookup.find(serviceName, connectionFactoryProvider.get()) >> ConnectionFactoriesByPriority.of(cacheMap)
        def failMessage = 'Connection is fail'
        def someServiceReturn = new ServiceReturn(null, null, null, 0)
        when:
        def result = tpCaller.tpcall(serviceName, data, flags, lookupService)

        then:
        (priorities*entriesPerPriority) * conFacHigh.getConnection() >> {throw new javax.resource.ResourceException(failMessage)}
        (1) * conLow.tpcall(serviceName, data, flags) >> someServiceReturn
        result == someServiceReturn
    }

    def 'cached entry, TPENOENT, should clear cache and lookup again'()
    {
       given:
       connectionFactoryProvider = Mock(ConnectionFactoryEntryStore)
       connectionFactoryProvider.get() >> [
               ConnectionFactoryEntry.of(connectionFactoryProducerLow)
       ]
       lookupService.connectionFactoryProvider = connectionFactoryProvider
       2 * lookup.find(serviceName, connectionFactoryProvider.get()) >> {
          def hit = ConnectionFactoriesByPriority.of([
                  (priorityLow): [ConnectionFactoryEntry.of(connectionFactoryProducerLow)]
          ])
          hit.setResolved(connectionFactoryProducerLow.getJndiName())
          return hit
       }
       def someServiceReturn = new ServiceReturn(null, null, null, 0)
       def tpenoentServiceReturn = new ServiceReturn(null, null, ErrorState.TPENOENT, 0)

       when:
       def result = tpCaller.tpcall(serviceName, data, flags, lookupService)
       def subsequentResult = tpCaller.tpcall(serviceName, data, flags, lookupService)
       then:
       3 * conLow.tpcall(serviceName, data, flags) >>> [someServiceReturn, tpenoentServiceReturn, someServiceReturn]
       !cache.get(serviceName).empty
       result == someServiceReturn
       subsequentResult == someServiceReturn
    }
}
