/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config


import spock.lang.Specification

class ReverseInboundTest extends Specification
{
    String host = "127.0.0.1"
    int port = 7894
    int size = 5
    long backoff = 123L

    ReverseInbound instance

    def setup()
    {
        instance = ReverseInbound.newBuilder().withHost( host )
                .withPort( port )
                .withSize( size )
                .withMaxConnectionBackoffMillis( backoff )
                .build()
    }

    def "Create then retrieve."()
    {
        expect:
        instance.getHost() == host
        instance.getPort() == port
        instance.getSize() == size
        instance.getMaxConnectionBackoffMillis() == backoff
    }

    def "equals and hashcode checks."()
    {
        when:
        ReverseInbound instance2 = ReverseInbound.newBuilder( instance ).build()
        ReverseInbound instance3 = ReverseInbound.newBuilder( instance ).withSize( size +1 ).build()

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
        String actual = instance.toString()

        then:
        actual.contains( host )
        actual.contains( "" + port )
        actual.contains( "" + size )
        actual.contains( "" + backoff )
    }
}
