/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller


import se.laz.casual.api.queue.QueueInfo
import se.laz.casual.api.service.ServiceDetails
import se.laz.casual.jca.CasualConnection
import se.laz.casual.jca.CasualConnectionFactory
import se.laz.casual.network.messages.domain.TransactionType
import spock.lang.Shared
import spock.lang.Specification

import javax.naming.InitialContext

class LookupTest extends Specification
{
    @Shared
    Lookup instance
    @Shared
    InitialContext ctx
    @Shared
    CasualConnectionFactory conFac
    @Shared
    CasualConnectionFactory conFacTwo
    @Shared
    CasualConnection con
    @Shared
    CasualConnection conTwo
    @Shared
    def jndiNameOne = 'eis/casualConnectionFactory'
    @Shared
    def jndiNameTwo = 'eis/anotherCasualConnectionFactory'
    @Shared
    ConnectionFactoryProducer producerOne = {
       def mock = Mock(ConnectionFactoryProducer)
       mock.getConnectionFactory() >> {
          conFac
       }
       mock.getJndiName() >> {
          jndiNameOne
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
         jndiNameTwo
      }
      return mock
   }()
    @Shared
    QueueInfo qinfo = QueueInfo.of('oddball.raccoon')
    @Shared
    def serviceName = 'casual.echo'
    @Shared
    def priority = 13L

    def setup()
    {
        instance = new Lookup()
        con = Mock(CasualConnection)
        conTwo = Mock(CasualConnection)
        conFac = Mock(CasualConnectionFactory)
        conFacTwo = Mock(CasualConnectionFactory)
        conFac.getConnection() >> con
        conFacTwo.getConnection() >> conTwo
    }

    def 'find CacheEntry using qinfo - should find it'()
    {
        setup:
        def cacheEntries = [ConnectionFactoryEntry.of(producerOne), ConnectionFactoryEntry.of(producerTwo)]
        con.queueExists(qinfo) >> false
        conTwo.queueExists(qinfo) >> true
        when:
        def entries = instance.find(qinfo, cacheEntries)
        then:
        !entries.isEmpty()
        entries[0].jndiName == jndiNameTwo
    }


    def 'find CacheEntry name using qinfo - should not find it'()
    {
        setup:
        def cacheEntries = [ConnectionFactoryEntry.of(producerOne), ConnectionFactoryEntry.of(producerTwo)]
        con.queueExists(qinfo) >> false
        conTwo.queueExists(qinfo) >> false
        when:
        def entries = instance.find(qinfo, cacheEntries)
        then:
        entries.isEmpty()
    }

    def 'find jndi name using serviceinfo - should find it'()
    {
        setup:
        def cacheEntries = [ConnectionFactoryEntry.of(producerOne), ConnectionFactoryEntry.of(producerTwo)]
        con.serviceDetails(serviceName) >> []
        conTwo.serviceDetails(serviceName) >> [new ServiceDetails(serviceName, "", TransactionType.NONE, 0L, priority)]
        con.serviceExists(serviceName) >> false
        conTwo.serviceExists(serviceName) >> true
        when:
        def entries = instance.find(serviceName, cacheEntries)
        then:
        !entries.isEmpty()
        entries.getForPriority(priority)[0].jndiName == jndiNameTwo
    }

    def 'find jndi name using serviceinfo - should not find it'()
    {
        setup:
        def cacheEntries = [ConnectionFactoryEntry.of(producerOne), ConnectionFactoryEntry.of(producerTwo)]
        con.serviceDetails(serviceName) >> []
        conTwo.serviceDetails(serviceName) >> []
        con.serviceExists(serviceName) >> false
        conTwo.serviceExists(serviceName) >> false
        when:
        def entries = instance.find(serviceName, cacheEntries)
        then:
        entries.isEmpty()
    }
}
