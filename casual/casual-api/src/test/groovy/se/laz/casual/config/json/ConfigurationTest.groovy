/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json

import se.laz.casual.api.external.json.JsonProviderFactory
import spock.lang.Specification

class ConfigurationTest extends Specification
{
    Inbound inbound = Inbound.newBuilder().build(  )
    Domain domain = Domain.newBuilder().build()
    Outbound outbound = Outbound.newBuilder().build()
    List<ReverseInbound> reverseInbounds = [ReverseInbound.newBuilder().build(  )]
    EventServer eventServer = EventServer.newBuilder().build()
    boolean epoll = true
    boolean unmanaged = true

    Configuration instance

    def setup()
    {
        instance = Configuration.newBuilder().withUseEpoll( epoll )
                .withUnmanaged( unmanaged )
                .withEventServer( eventServer )
                .withReverseInbound( reverseInbounds )
                .withOutbound( outbound )
                .withDomain( domain )
                .withInbound( inbound )
        .build()
    }

    def "Create then retrieve."()
    {
        expect:
        instance.getUseEpoll(  ) == epoll
        instance.getUnmanaged(  ) == unmanaged
        instance.getInbound( ) == inbound
        instance.getOutbound(  ) == outbound
        instance.getReverseInbound( ) == reverseInbounds
        instance.getEventServer(  ) == eventServer
        instance.getDomain() == domain
    }

    def "equals and hashcode check."()
    {
        when:
        Configuration instance2 = Configuration.newBuilder(instance).build()
        Configuration instance3 = Configuration.newBuilder(instance).withUseEpoll( !epoll ).build()

        then:
        instance == instance
        instance2 == instance
        instance3 != instance
        instance.hashCode(  ) == instance.hashCode(  )
        instance2.hashCode(  ) == instance.hashCode(  )
        instance3.hashCode( ) != instance.hashCode(  )
        !instance.equals( "String" )
    }

    def "to String check."()
    {
        when:
        String actual = instance.toString(  )

        then:
        actual.contains( inbound.toString(  ) )
        actual.contains( outbound.toString(  ) )
        actual.contains( reverseInbounds.toString(  ) )
        actual.contains( eventServer.toString(  ) )
        actual.contains( domain.toString(  ) )
        actual.contains( "" +  epoll )
        actual.contains( "" + unmanaged )
    }

    def "Json roundtrip check."()
    {
        given:
        String json = JsonProviderFactory.getJsonProvider(  ).toJson( instance )

        when:
        Configuration actual = JsonProviderFactory.getJsonProvider(  ).fromJson( json, Configuration.class )

        then:
        actual == instance
    }
}
