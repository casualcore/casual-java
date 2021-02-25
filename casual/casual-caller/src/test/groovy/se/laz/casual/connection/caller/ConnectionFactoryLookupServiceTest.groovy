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

class ConnectionFactoryLookupServiceTest extends Specification
{
    @Shared
    def jndiNameConFactoryOne = 'eis/casualConnectionFactory'
    @Shared
    def jndiNameConFactoryTwo = 'eis/anotherCasualConnectionFactory'
    @Shared
    QueueInfo qinfo = QueueInfo.createBuilder().withQspace('oddball').withQname('raccoon').build()
    @Shared
    def serviceName = 'casual.echo'
    @Shared
    ConnectionFactoryProvider connnectionFactoryProvider
    @Shared
    Cache cache
    @Shared
    Lookup lookup
    @Shared
    CasualConnectionFactory conFac
    @Shared
    CasualConnectionFactory conFacTwo
    @Shared
    def env = new HashMap()
    @Shared
    ConnectionFactoryLookupService instance

    def setup()
    {
        conFac = Mock(CasualConnectionFactory)
        conFacTwo = Mock(CasualConnectionFactory)
        connnectionFactoryProvider = Mock(ConnectionFactoryProvider)
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
            [ConnectionFactoryEntry.of(jndiNameConFactoryOne, conFac), ConnectionFactoryEntry.of(jndiNameConFactoryTwo, conFacTwo)] as List<ConnectionFactoryEntry>
        }
        expect:
        def entries = connnectionFactoryProvider.get();
        entries.size() == 2
        instance.connectionFactoryProvider == connnectionFactoryProvider
        instance.cache == cache
        instance.lookup == lookup
    }

    def 'qinfo get cached entry name, no cached entry'()
    {
        given:
        ConnectionFactoryEntry entry = ConnectionFactoryEntry.of(jndiNameConFactoryTwo, conFacTwo)
        lookup.find(qinfo, _) >> [entry]
        when:
        def entries = instance.get(qinfo)
        then:
        entries.size() == 1
        entries[0] == entry
    }

    def 'qinfo get jndi name, no cached entry - not found'()
    {
        setup:
        lookup.find(qinfo, _) >> []
        when:
        def entries = instance.get(qinfo)
        then:
        entries.isEmpty()
    }

    def 'qinfo get jndi name, cached entry'()
    {
        setup:
        cache.store(qinfo, [ConnectionFactoryEntry.of(jndiNameConFactoryTwo, Mock(CasualConnectionFactory))])
        when:
        def entries = instance.get(qinfo)
        then:
        entries.size() == 1
        entries[0].jndiName == jndiNameConFactoryTwo
        0 * lookup.find(qinfo, _)
    }

    def 'service info get jndi name, no cached entry'()
    {
        setup:
        ConnectionFactoryEntry entry = ConnectionFactoryEntry.of(jndiNameConFactoryTwo, conFacTwo)
        lookup.find(serviceName, _) >> [entry]
        when:
        def entries = instance.get(serviceName)
        then:
        entries.size() == 1
        entries[0] == entry
    }

    def 'service info get jndi name, no cached entry - not found'()
    {
        setup:
        lookup.find(serviceName, _) >> []
        when:
        def entries = instance.get(serviceName)
        then:
        entries.isEmpty()
    }

    def 'service get jndi name, cached entry'()
    {
        setup:
        ConnectionFactoryEntry entry = ConnectionFactoryEntry.of(jndiNameConFactoryTwo, conFacTwo)
        cache.store(serviceName, [entry])
        when:
        def entries = instance.get(serviceName)
        then:
        entries.size() == 1
        entries[0] == entry
        0 * lookup.find(serviceName, _)
    }
}
