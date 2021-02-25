/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller

import se.laz.casual.api.queue.QueueInfo
import se.laz.casual.jca.CasualConnectionFactory
import spock.lang.Shared
import spock.lang.Specification

class CacheTest extends Specification
{
    @Shared
    Cache instance
    @Shared
    def connectionFactoryOne = Mock(CasualConnectionFactory)
    @Shared
    def jndiNameOne = 'eis/CasualConnectionFactory'
    @Shared
    def connectionFactoryTwo = Mock(CasualConnectionFactory)
    @Shared
    def jndiNameTwo = 'eis/AnotherCasualConnectionFactory'
    @Shared
    def cacheEntryOne = ConnectionFactoryEntry.of(jndiNameOne, connectionFactoryOne)
    @Shared
    def cacheEntryTwo = ConnectionFactoryEntry.of(jndiNameTwo, connectionFactoryTwo)
    @Shared
    def serviceName = 'casual.test.echo'
    @Shared
    def qInfo = QueueInfo.createBuilder().withQspace('space1').withQname('agrajag').build()
    @Shared
    def qInfoList = [QueueInfo.createBuilder().withQspace('hairy').withQname('otter').build(), QueueInfo.createBuilder().withQspace('drunken').withQname('monkey').build()]
    @Shared
    def serviceNames = ['casual.rollback', 'casually.casual']

    def setup()
    {
        instance = new Cache()
        qInfoList.forEach({q -> instance.store(q, [cacheEntryOne])})
        serviceNames.forEach({ s -> instance.store(s, [cacheEntryTwo])})
        instance.store(serviceName, [cacheEntryOne, cacheEntryTwo])
    }

    def 'store null cache entry'()
    {
        when:
        instance.store(serviceName, null)
        then:
        thrown(NullPointerException)
    }

    def 'set and get get service'()
    {
        given:
        def anotherServiceName = 'anotherServiceName'
        when:
        instance.store(anotherServiceName, [cacheEntryOne, cacheEntryTwo])
        def entries = instance.get(anotherServiceName)
        then:
        entries.size() == 2
    }

    def 'get missing service entry'()
    {
        when:
        def entries = instance.get('does-not-exist')
        then:
        entries.isEmpty()
    }

    def 'store queue null cache entry'()
    {
        when:
        instance.store(qInfo, null)
        then:
        thrown(NullPointerException)
    }

    def 'set and get queue'()
    {
        when:
        instance.store(qInfo, [cacheEntryOne])
        def entries = instance.get(qInfo)
        then:
        entries.size() == 1
    }

    def 'get missing queue entry'()
    {
        given:
        def qinfoNotStored = QueueInfo.createBuilder().withQspace("abc").withQname("Ford Prefect").build()
        when:
        def entries = instance.get(qinfoNotStored)
        then:
        entries.isEmpty()
    }

}
