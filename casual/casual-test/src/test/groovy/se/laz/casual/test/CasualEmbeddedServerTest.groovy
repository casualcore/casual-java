/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test


import spock.lang.Specification

class CasualEmbeddedServerTest extends Specification
{
    CasualEmbeddedServer instance

    def setup()
    {
        instance = CasualEmbeddedServer.newBuilder().build()
    }

    def cleanup()
    {
        if ( instance != null && instance.isRunning() )
        {
            instance.shutdown()
        }
    }

    def "Create then get"()
    {
        expect:
        !instance.isRunning()
        !instance.isEventServerEnabled()
        !instance.isEventServerRunning()
        instance.getEventServerPort().isEmpty()
    }

    def "Start and then shutdown."()
    {
        expect:
        !instance.isRunning()

        when:
        instance.start()

        then:
        instance.isRunning()

        when:
        instance.shutdown()

        then:
        !instance.isRunning()
    }

    def "Shutdown can always be called."()
    {
        when:
        instance.shutdown()

        then:
        !instance.isRunning()
        noExceptionThrown()

        when:
        instance.shutdown()

        then:
        !instance.isRunning()
        noExceptionThrown()
    }

    def "Start can only be called once."()
    {
        when:
        instance.start()

        then:
        instance.isRunning()

        when:
        instance.start()

        then:
        thrown IllegalStateException
        instance.isRunning()
    }

    def "Start with event server enabled."()
    {
        given:
        CasualEmbeddedServer instance2 = CasualEmbeddedServer.newBuilder( instance )
                .eventServerEnabled( true )
                .build()

        when:
        instance2.start()

        then:
        instance2.isRunning()
        instance2.isEventServerRunning()
        instance2.getEventServerPort().isPresent()
    }
}
