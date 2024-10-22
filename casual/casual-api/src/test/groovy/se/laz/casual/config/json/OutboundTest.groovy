/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json

import se.laz.casual.api.external.json.JsonProviderFactory
import spock.lang.Specification

class OutboundTest extends Specification
{
    String executorName = "bob"
    int threads = 2
    boolean unmanaged = true
    boolean epoll = true

    Outbound instance

    def setup()
    {
        instance = Outbound.newBuilder().withManagedExecutorServiceName( executorName ).withNumberOfThreads( threads )
                .withUnmanaged( unmanaged ).withUseEpoll( epoll ).build()
    }

    def "Create then retrieve."()
    {
        expect:
        instance.getManagedExecutorServiceName() == executorName
        instance.getNumberOfThreads() == threads
        instance.getUnmanaged() == unmanaged
        instance.getUseEpoll() == epoll
    }

    def "equals and hashcode checks"()
    {
        when:
        Outbound instance2 = Outbound.newBuilder( instance ).build()
        Outbound instance3 = Outbound.newBuilder( instance ).withNumberOfThreads( threads + 1 ).build()

        then:
        instance == instance
        instance2 == instance
        instance3 != instance
        instance.hashCode() == instance.hashCode()
        instance2.hashCode() == instance.hashCode()
        instance3.hashCode() != instance.hashCode()
        !instance.equals( "String" )
    }

    def "to String check."()
    {
        when:
        String actual = instance.toString()

        then:
        actual.contains( executorName )
        actual.contains( "" + threads )
        actual.contains( "" + unmanaged )
        actual.contains( "" + epoll )
    }

    def "Json roundtrip check."()
    {
        given:
        String json = JsonProviderFactory.getJsonProvider().toJson( instance )

        when:
        Outbound actual = JsonProviderFactory.getJsonProvider().fromJson( json, Outbound.class )

        then:
        actual == instance
    }

}
