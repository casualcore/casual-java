/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.lookup

import se.laz.casual.api.queue.QueueInfo
import se.laz.casual.api.service.ServiceInfo
import se.laz.casual.jca.CasualConnection
import se.laz.casual.jca.CasualConnectionFactory
import spock.lang.Shared
import spock.lang.Specification

import javax.naming.InitialContext
import javax.naming.NamingException

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
    def jndiNames = [jndiNameOne, jndiNameTwo]
    @Shared
    QueueInfo qinfo = QueueInfo.createBuilder().withQspace('oddball').withQname('raccoon').build()
    @Shared
    ServiceInfo serviceInfo = ServiceInfo.of('casual.echo')

    def setup()
    {
        instance = new Lookup()
        ctx = Mock(InitialContext)
        con = Mock(CasualConnection)
        conTwo = Mock(CasualConnection)
        conFac = Mock(CasualConnectionFactory)
        conFacTwo = Mock(CasualConnectionFactory)
        conFac.getConnection() >> con
        conFacTwo.getConnection() >> conTwo
    }

    def 'lookup fail'()
    {
        setup:
        ctx.lookup(_) >> {throw new NamingException()}
        when:
        instance.findJNDIName(qinfo, ctx, jndiNames)
        then:
        def e = thrown(CasualLookupException)
        e.getCause().getClass() == NamingException
    }

    def 'find jndi name using qinfo - should find it'()
    {
        setup:
        ctx.lookup(jndiNameOne) >> conFac
        ctx.lookup(jndiNameTwo) >> conFacTwo
        con.queueExists(qinfo) >> false
        conTwo.queueExists(qinfo) >> true
        when:
        def answer = instance.findJNDIName(qinfo, ctx, jndiNames)
        then:
        noExceptionThrown()
        answer.isPresent()
        answer.get() == jndiNameTwo
    }


    def 'find jndi name using qinfo - should not find it'()
    {
        setup:
        ctx.lookup(jndiNameOne) >> conFac
        ctx.lookup(jndiNameTwo) >> conFacTwo
        con.queueExists(qinfo) >> false
        conTwo.queueExists(qinfo) >> false
        when:
        def answer = instance.findJNDIName(qinfo, ctx, jndiNames)
        then:
        noExceptionThrown()
        !answer.isPresent()
    }

    def 'find jndi name using serviceinfo - should find it'()
    {
        setup:
        ctx.lookup(jndiNameOne) >> conFac
        ctx.lookup(jndiNameTwo) >> conFacTwo
        con.serviceExists(serviceInfo.getServiceName()) >> false
        conTwo.serviceExists(serviceInfo.getServiceName()) >> true
        when:
        def answer = instance.findJNDIName(serviceInfo, ctx, jndiNames)
        then:
        noExceptionThrown()
        answer.isPresent()
        answer.get() == jndiNameTwo
    }

    def 'find jndi name using serviceinfo - should not find it'()
    {
        setup:
        ctx.lookup(jndiNameOne) >> conFac
        ctx.lookup(jndiNameTwo) >> conFacTwo
        con.serviceExists(serviceInfo.getServiceName()) >> false
        conTwo.serviceExists(serviceInfo.getServiceName()) >> false
        when:
        def answer = instance.findJNDIName(serviceInfo, ctx, jndiNames)
        then:
        noExceptionThrown()
        !answer.isPresent()
    }
}
