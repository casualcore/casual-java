/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test


import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class CasualEmbeddedServerTest extends Specification
{
    @Shared
    int eventServerPort = 7772

    CasualEmbeddedServer instance

    def setup()
    {
        instance = CasualEmbeddedServer.newBuilder().eventServerPort( eventServerPort ).build()
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
        instance.getEventServerPort() == eventServerPort
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

    def "Start and connect to event server, disconnect, shutdown"()
    {
        given:
        instance.start()
        InetSocketAddress address = new InetSocketAddress( eventServerPort )

        when:
        SocketChannel socketChannel = SocketChannel.open( address )
        byte[] payload = '{"message":"HELLO"}' as byte[]
        ByteBuffer buffer = ByteBuffer.wrap( payload )
        socketChannel.write( buffer )

        then:
        noExceptionThrown()
    }
}
