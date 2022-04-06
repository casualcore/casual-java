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
    CasualConnectionFactoryProducer producerOne = {
      def mock = Mock(CasualConnectionFactoryProducer)
      mock.getConnectionFactory() >> {
         connectionFactoryOne
      }
      mock.getJndiName() >> {
         jndiNameOne
      }
      return mock
    }()
    @Shared
    CasualConnectionFactoryProducer producerTwo = {
       def mock = Mock(CasualConnectionFactoryProducer)
       mock.getConnectionFactory() >> {
          connectionFactoryTwo
       }
       mock.getJndiName() >> {
          jndiNameTwo
       }
       return mock
    }()
    @Shared
    def cacheEntryOne = ConnectionFactoryEntry.of(producerOne)
    @Shared
    def cacheEntryTwo = ConnectionFactoryEntry.of(producerTwo)
    @Shared
    def serviceName = 'casual.test.echo'
    @Shared
    def qInfo = QueueInfo.createBuilder().withQueueName('space1.agrajag').build()
    @Shared
    def qInfoList = [QueueInfo.createBuilder().withQueueName('hairy.otter').build(), QueueInfo.createBuilder().withQueueName('drunken.monkey').build()]
    @Shared
    def serviceNames = ['casual.rollback', 'casually.casual']
    @Shared
    def priority = 17L
    @Shared
    def priorityMapping

    def setup()
    {
        instance = new Cache()
        qInfoList.forEach({q -> instance.store(q, [cacheEntryOne])})
        serviceNames.forEach({ s -> instance.store(s, ConnectionFactoriesByPriority.of([(priority): [cacheEntryTwo]]))})
        instance.store(serviceName, ConnectionFactoriesByPriority.of([(priority): [cacheEntryOne, cacheEntryTwo]]))
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
        instance.store(anotherServiceName, ConnectionFactoriesByPriority.of([(priority): [cacheEntryOne, cacheEntryTwo]]))
        def entries = instance.get(anotherServiceName)
        then:
        entries.getForPriority(priority).size() == 2
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
        def qinfoNotStored = QueueInfo.createBuilder().withQueueName("abc.Ford Prefect").build()
        when:
        def entries = instance.get(qinfoNotStored)
        then:
        entries.isEmpty()
    }

}
