/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.lookup

import se.laz.casual.api.queue.QueueInfo
import se.laz.casual.api.service.ServiceInfo
import spock.lang.Shared
import spock.lang.Specification

class CacheTest extends Specification
{
    @Shared
    def instance
    @Shared
    def jndiName = 'eis/CasualConnectionFactory'
    @Shared
    def serviceInfo = ServiceInfo.of('casual.test.echo')
    @Shared
    def qInfo = QueueInfo.createBuilder().withQspace('space1').withQname('agrajag').build()
    @Shared
    def qInfoList = [QueueInfo.createBuilder().withQspace('hairy').withQname('otter').build(), QueueInfo.createBuilder().withQspace('drunken').withQname('monkey').build()]
    @Shared
    def serviceInfoList = [ServiceInfo.of('casual.rollback'), ServiceInfo.of('casually.casual')]

    def setup()
    {
        instance = new Cache()
        qInfoList.forEach({q -> instance.setJNDIName(q, jndiName)})
        serviceInfoList.forEach({s -> instance.setJNDIName(s, jndiName)})
    }

    def 'set service null jndi name'()
    {
        when:
        instance.setJNDIName(serviceInfo, null)
        then:
        def e = thrown(NullPointerException)
        e.message == 'jndi can not be null'
    }

    def 'set and get service'()
    {
        when:
        instance.setJNDIName(serviceInfo, jndiName)
        def jndi = instance.getJNDIName(serviceInfo).get()
        then:
        noExceptionThrown()
        jndi == jndiName
    }

    def 'get missing service entry'()
    {
        when:
        def jndi = instance.getJNDIName(serviceInfo)
        then:
        noExceptionThrown()
        !jndi.isPresent()
    }

    def 'set queue null jndi name'()
    {
        when:
        instance.setJNDIName(qInfo, null)
        then:
        def e = thrown(NullPointerException)
        e.message == 'jndi can not be null'
    }

    def 'set and get queue'()
    {
        when:
        instance.setJNDIName(qInfo, jndiName)
        def jndi = instance.getJNDIName(qInfo).get()
        then:
        noExceptionThrown()
        jndi == jndiName
    }

    def 'get missing queue entry'()
    {
        when:
        def jndi = instance.getJNDIName(qInfo)
        then:
        noExceptionThrown()
        !jndi.isPresent()
    }

    def 'evict qinfo'()
    {
        setup:
        instance.setJNDIName(qInfo, jndiName)
        when:
        instance.evict(qInfo)
        def r = instance.getJNDIName(qInfo)
        then:
        noExceptionThrown()
        !r.isPresent()
    }

    def 'evict qinfo - emtpy cache'()
    {
        when:
        instance.evict(qInfo)
        def r = instance.getJNDIName(qInfo)
        then:
        noExceptionThrown()
        !r.isPresent()
    }

    def 'evict service info'()
    {
        setup:
        instance.setJNDIName(serviceInfo, jndiName)
        when:
        instance.evict(serviceInfo)
        def r = instance.getJNDIName(serviceInfo)
        then:
        noExceptionThrown()
        !r.isPresent()
    }

    def 'evict service info - empty cache'()
    {
        when:
        instance.evict(serviceInfo)
        def r = instance.getJNDIName(serviceInfo)
        then:
        noExceptionThrown()
        !r.isPresent()
    }

}
