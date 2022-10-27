/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller

import se.laz.casual.api.queue.QueueInfo
import se.laz.casual.jca.CasualConnection
import se.laz.casual.jca.CasualConnectionFactory
import spock.lang.Shared
import spock.lang.Specification

class ConnectionFactoryLookupServiceTest extends Specification
{
    @Shared
    def jndiNameConFactoryOne = 'eis/casualConnectionFactory'
    @Shared
    def jndiNameConFactoryTwo = 'eis/anotherCasualConnectionFactory'
    @Shared
    QueueInfo qinfo = QueueInfo.of('oddball.raccoon')
    @Shared
    def serviceName = 'casual.echo'
    @Shared
    ConnectionFactoryEntryStore connnectionFactoryProvider
    @Shared
    Cache cache
    @Shared
    Lookup lookup
    @Shared
    CasualConnectionFactory conFac
    @Shared
    CasualConnectionFactory conFacTwo
    @Shared
    ConnectionFactoryProducer producerOne = {
       def mock = Mock(ConnectionFactoryProducer)
       mock.getConnectionFactory() >> {
          conFac
       }
      mock.getJndiName() >> {
         jndiNameConFactoryOne
      }
      return mock
    }()
    @Shared
    ConnectionFactoryProducer producerTwo = {
       def mock = Mock(ConnectionFactoryProducer)
       mock.getConnectionFactory() >> {
          conFacTwo
       }
       mock.getJndiName() >> {
          jndiNameConFactoryTwo
       }
       return mock
    }()

    @Shared
    def env = new HashMap()
    @Shared
    ConnectionFactoryLookupService instance
    @Shared
    def priority = 3L
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

    def setup()
    {
        conHigh = Mock(CasualConnection)
        conLow = Mock(CasualConnection)
        conFacHigh = Mock(CasualConnectionFactory)
        conFacLow = Mock(CasualConnectionFactory)
        conFacHigh.getConnection() >> conHigh
        conFacLow.getConnection() >> conLow

        conFac = Mock(CasualConnectionFactory)
        conFacTwo = Mock(CasualConnectionFactory)
        connnectionFactoryProvider = Mock(ConnectionFactoryEntryStore)
        cache =  new Cache()
        lookup = Mock(Lookup)
        instance = new ConnectionFactoryLookupService()
        instance.connectionFactoryProvider = connnectionFactoryProvider
        instance.cache = cache
        instance.lookup = lookup
    }

    def 'asssert basic sanity'()
    {
        given:
        connnectionFactoryProvider.get() >> {
            [ConnectionFactoryEntry.of(producerOne), ConnectionFactoryEntry.of(producerTwo)] as List<ConnectionFactoryEntry>
        }
        expect:
        def entries = connnectionFactoryProvider.get()
        entries.size() == 2
        instance.connectionFactoryProvider == connnectionFactoryProvider
        instance.cache == cache
        instance.lookup == lookup
    }

    def 'qinfo get cached entry name, no cached entry'()
    {
        given:
        ConnectionFactoryEntry entry = ConnectionFactoryEntry.of(producerTwo)
        lookup.find(qinfo, _) >> [entry]
        when:
        def actual = instance.get(qinfo)
        then:
        actual.isPresent()
        actual.get() == entry
    }

    def 'qinfo get jndi name, no cached entry - not found'()
    {
        setup:
        lookup.find(qinfo, _) >> []
        when:
        def entry = instance.get(qinfo)
        then:
        !entry.isPresent()
    }

    def 'qinfo get jndi name, cached entry'()
    {
        setup:
        cache.store(qinfo, [ConnectionFactoryEntry.of(producerTwo)])
        when:
        def entry = instance.get(qinfo)
        then:
        entry.isPresent()
        entry.get().jndiName == jndiNameConFactoryTwo
        0 * lookup.find(qinfo, _)
    }

    def 'service info get jndi name, no cached entry'()
    {
        setup:
        ConnectionFactoryEntry entry = ConnectionFactoryEntry.of(producerTwo)
        connnectionFactoryProvider.get() >> [entry]
        lookup.find(serviceName, _) >> ConnectionFactoriesByPriority.of([(priority): [entry]])
        when:
        def entries = instance.get(serviceName)
        then:
        entries.size() == 1
        entries[0] == entry
    }

    def 'service info get jndi name, no cached entry - not found'()
    {
        setup:
        connnectionFactoryProvider.get() >> []
        lookup.find(serviceName, _) >> ConnectionFactoriesByPriority.of([:])
        when:
        def entries = instance.get(serviceName)
        then:
        entries.isEmpty()
    }

    def 'service get jndi name, cached entry'()
    {
        setup:
        ConnectionFactoryEntry entry = ConnectionFactoryEntry.of(producerTwo)
        connnectionFactoryProvider.get() >> [entry]
        cache.store(serviceName, ConnectionFactoriesByPriority.of([(priority): [entry]], [entry.getJndiName()]))
        when:
        def entries = instance.get(serviceName)
        then:
        entries.size() == 1
        entries[0] == entry
        0 * lookup.find(serviceName, _)
    }

    def "order is randomized"()
    {
        setup:
        def entriesPerPriority = 128
        Map<Long, List<ConnectionFactoryEntry>> lookupMap = [:]

        List<ConnectionFactoryEntry> listOfEntries = []
        for (int i = 0; i < entriesPerPriority; i++)
        {
            ConnectionFactoryProducer producer = Mock(ConnectionFactoryProducer){
               getJndiName() >> {
                  "jndi_index_"+i
               }
               getConnectionFactory() >> {
                  conFacHigh
               }
            }
            listOfEntries.add(ConnectionFactoryEntry.of(producer))
        }
        lookupMap.put(1L, listOfEntries)

        connnectionFactoryProvider.get() >> listOfEntries

        lookup.find(serviceName, _) >> ConnectionFactoriesByPriority.of(lookupMap)

        when:
        def result1 = instance.get(serviceName)
        def result2 = instance.get(serviceName)

        then:
        // entries are shuffeled
        result1 != result2

        // .. but they should still contains all the same items
        result1.sort() == result2.sort()
    }

    def "priority is descending"()
    {
        setup:
        def conFac1 = Mock(CasualConnectionFactory)
        def conFac2 = Mock(CasualConnectionFactory)
        def conFac3 = Mock(CasualConnectionFactory)
        def conFac4 = Mock(CasualConnectionFactory)

        def conFac1Name = "name1"
        def conFac2Name = "name2"
        def conFac3Name = "name3"
        def conFac4Name = "name4"

        def producerOneLocal = Mock(ConnectionFactoryProducer){
           getConnectionFactory() >> {
              conFac1
           }
           getJndiName() >> {
              conFac1Name
           }
        }
        def producerTwoLocal = Mock(ConnectionFactoryProducer){
           getConnectionFactory() >> {
              conFac2
           }
           getJndiName() >> {
              conFac2Name
           }
        }
        def producerThreeLocal = Mock(ConnectionFactoryProducer){
           getConnectionFactory() >> {
              conFac3
           }
           getJndiName() >> {
              conFac3Name
           }
        }
        def producerFourLocal = Mock(ConnectionFactoryProducer){
           getConnectionFactory() >> {
              conFac4
           }
           getJndiName() >> {
              conFac4Name
           }
        }


        def conFac1Entry = ConnectionFactoryEntry.of(producerOneLocal)
        def conFac2Entry = ConnectionFactoryEntry.of(producerTwoLocal)
        def conFac3Entry = ConnectionFactoryEntry.of(producerThreeLocal)
        def conFac4Entry = ConnectionFactoryEntry.of(producerFourLocal)

        connnectionFactoryProvider.get() >> [conFac1Entry, conFac2Entry, conFac3Entry, conFac4Entry]
        lookup.find(serviceName, _) >> ConnectionFactoriesByPriority.of([
                (3L): [conFac1Entry],
                (2L): [conFac2Entry],
                (1L): [conFac3Entry],
                (0L): [conFac4Entry],
        ])

        when:
        def result = instance.get(serviceName)

        then:
        result[0].getConnectionFactory() == conFac4
        result[1].getConnectionFactory() == conFac3
        result[2].getConnectionFactory() == conFac2
        result[3].getConnectionFactory() == conFac1
    }
}
