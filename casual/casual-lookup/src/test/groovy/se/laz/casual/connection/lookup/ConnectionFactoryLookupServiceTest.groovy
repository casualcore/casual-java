/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.lookup

import se.laz.casual.api.queue.QueueInfo
import se.laz.casual.api.service.ServiceInfo
import se.laz.casual.jca.CasualConnectionFactory
import spock.lang.Shared
import spock.lang.Specification

class ConnectionFactoryLookupServiceTest extends Specification
{
    @Shared
    def resource = 'casual-lookup-config.json'
    @Shared
    def jndiNameConFactoryOne = 'eis/casualConnectionFactory'
    @Shared
    def jndiNameConFactoryTwo = 'eis/anotherCasualConnectionFactory'
    @Shared
    QueueInfo qinfo = QueueInfo.createBuilder().withQspace('oddball').withQname('raccoon').build()
    @Shared
    ServiceInfo serviceInfo = ServiceInfo.of('casual.echo')
    @Shared
    ConfigurationProvider config = new ConfigurationProvider()
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

    def setupSpec()
    {
        Properties p = System.getProperties()
        p.setProperty(ConfigurationProvider.SYSTEM_PROPERTY_NAME, getClass().getClassLoader().getResource(resource).toURI().path)
        config.initialize()
    }

    def setup()
    {
        conFac = Mock(CasualConnectionFactory)
        conFacTwo = Mock(CasualConnectionFactory)
        cache =  new Cache()
        lookup = Mock(Lookup)
        instance = new ConnectionFactoryLookupService()
        instance.config = config
        instance.cache = cache
        instance.lookup = lookup
    }

    def 'asssert basic sanity'()
    {
        expect:
        config.getCasualJNDINames().size() == 2
        instance.config == config
        instance.cache == cache
        instance.lookup == lookup
    }

    def 'qinfo get jndi name, no cached entry'()
    {
        given:
        lookup.findJNDIName(qinfo, _, _) >> Optional.of(jndiNameConFactoryTwo)
        when:
        def r = instance.getJNDIName(qinfo, env)
        then:
        noExceptionThrown()
        r.isPresent()
        r.get() == jndiNameConFactoryTwo
    }

    def 'qinfo get jndi name, no cached entry - not found'()
    {
        setup:
        lookup.findJNDIName(qinfo, _, _) >> Optional.empty()
        when:
        def r = instance.getJNDIName(qinfo, env)
        then:
        noExceptionThrown()
        !r.isPresent()
    }

    def 'qinfo get jndi name, cached entry'()
    {
        setup:
        cache.setJNDIName(qinfo, jndiNameConFactoryTwo)
        when:
        def r = instance.getJNDIName(qinfo, env)
        then:
        noExceptionThrown()
        r.isPresent()
        r.get() == jndiNameConFactoryTwo
        0 * lookup.findJNDIName(qinfo, _, _)
    }

    def 'service info get jndi name, no cached entry'()
    {
        setup:
        lookup.findJNDIName(serviceInfo, _, _) >> Optional.of(jndiNameConFactoryTwo)
        when:
        def r = instance.getJNDIName(serviceInfo, env)
        then:
        noExceptionThrown()
        r.isPresent()
        r.get() == jndiNameConFactoryTwo
    }

    def 'service info get jndi name, no cached entry - not found'()
    {
        setup:
        lookup.findJNDIName(serviceInfo, _, _) >> Optional.empty()
        when:
        def r = instance.getJNDIName(serviceInfo, env)
        then:
        noExceptionThrown()
        !r.isPresent()
    }

    def 'service get jndi name, cached entry'()
    {
        setup:
        cache.setJNDIName(serviceInfo, jndiNameConFactoryTwo)
        when:
        def r = instance.getJNDIName(serviceInfo, env)
        then:
        noExceptionThrown()
        r.isPresent()
        r.get() == jndiNameConFactoryTwo
        0 * lookup.findJNDIName(serviceInfo, _, _)
    }

    def 'evict qinfo'()
    {
        setup:
        cache.setJNDIName(qinfo, jndiNameConFactoryOne)
        when:
        instance.evict(qinfo)
        def r = cache.getJNDIName(qinfo)
        then:
        noExceptionThrown()
        !r.isPresent()
    }

    def 'evict service info'()
    {
        setup:
        cache.setJNDIName(serviceInfo, jndiNameConFactoryOne)
        when:
        instance.evict(serviceInfo)
        def r = cache.getJNDIName(serviceInfo)
        then:
        noExceptionThrown()
        !r.isPresent()
    }

}
