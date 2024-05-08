/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.network

import spock.lang.Specification

class NetworkPortFactoryTest extends Specification
{
    def "Get available port multiple times."()
    {
        when:
        int actual = NetworkPortFactory.getAvailablePort()

        then:
        actual > 0

        and:
        int actual2 = NetworkPortFactory.getAvailablePort()

        then:
        actual2 > 0
        actual != actual2
    }

    def "Get available port then use, ask for port again."()
    {
        given:
        int availablePort = NetworkPortFactory.getAvailablePort()

        when:
        boolean available = NetworkPortFactory.isPortAvailable( availablePort )

        then:
        available

        when:
        ServerSocket socket = new ServerSocket( availablePort )

        then:
        socket.isBound()

        when:
        available = NetworkPortFactory.isPortAvailable( availablePort )

        then:
        !available

        cleanup:
        socket.close()
    }
}
