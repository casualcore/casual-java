/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller

import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.ServiceReturn
import se.laz.casual.api.flags.Flag
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

    TpCallerFailover tpCaller
    ConnectionFactoryProvider connectionFactoryProvider
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

        connectionFactoryProvider = Mock(ConnectionFactoryProvider)
        cache =  new Cache()
        lookup = Mock(Lookup)
        lookupService = new ConnectionFactoryLookupService()
        lookupService.connectionFactoryProvider = connectionFactoryProvider
        lookupService.cache = cache
        lookupService.lookup = lookup

        connectionFactoryProvider.get() >> [
                ConnectionFactoryEntry.of(conFacHighJndi, conFacHigh),
                ConnectionFactoryEntry.of(conFacLowJndi, conFacLow)
        ]
    }

    def "2 connection factories - first connection throws exception, second succeeds"()
    {
        setup:
        lookup.find(serviceName, connectionFactoryProvider.get()) >> ConnectionFactoriesByPriority.of([
                (priorityHigh): [ConnectionFactoryEntry.of(conFacHighJndi, conFacHigh)],
                (priorityLow): [ConnectionFactoryEntry.of(conFacLowJndi, conFacLow)]
        ])
        def failMessage = 'Connection is fail'

        def someServiceReturn = new ServiceReturn(null, null, null, 0)

        when:
        def result = tpCaller.tpcall(serviceName, data, flags, lookupService)

        then:
        1 * conFacHigh.getConnection() >> {throw new javax.resource.ResourceException(failMessage)}
        0 * conHigh.tpcall(serviceName, data, flags) >> {throw new RuntimeException("This should not happen because getConnection should fail")}
        1 * conLow.tpcall(serviceName, data, flags) >> someServiceReturn
        result == someServiceReturn
    }

    def "2 connection factories with same priority - both are called and fail, exception is thrown"()
    {
        setup:
        lookup.find(serviceName, connectionFactoryProvider.get()) >> ConnectionFactoriesByPriority.of([
                (priorityHigh): [ConnectionFactoryEntry.of(conFacHighJndi, conFacHigh), ConnectionFactoryEntry.of(conFacLowJndi, conFacLow)]
        ])
        def failMessage = 'Connection is fail'

        when:
        def result = tpCaller.tpcall(serviceName, data, flags, lookupService)

        then:
        1 * conFacHigh.getConnection() >> {throw new javax.resource.ResourceException(failMessage)}
        1 * conFacLow.getConnection() >> {throw new javax.resource.ResourceException(failMessage)}
        def e = thrown(CasualResourceException)
        e.message == 'Call failed to all 2 available casual connections connections.'
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
                listOfEntries.add(ConnectionFactoryEntry.of(conFacHighJndi+prioIndex+":"+i, conFacHigh))
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
        e.message == 'Call failed to all 64 available casual connections connections.'
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
                listOfEntries.add(ConnectionFactoryEntry.of(conFacHighJndi+prioIndex+":"+i, conFacHigh))
            }
            cacheMap.put(prioIndex, listOfEntries)
        }

        // Will succeed. Set of (entiresPerPriority) that will be called last, any should return OK.
        List<ConnectionFactoryEntry> listOfEntries = []
        for (int i = 0; i < entriesPerPriority; i++)
        {
            listOfEntries.add(ConnectionFactoryEntry.of(conFacHighJndi+(priorities+1L)+":"+i, conFacLow))
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
}
