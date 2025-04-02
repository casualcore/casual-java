/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.connection

import io.fabric8.kubernetes.client.LocalPortForward
import spock.lang.Specification

class LocalConnectionTest extends Specification
{

    PortForwardedConnection instance

    LocalPortForward portForward = Mock()

    def setup()
    {
        instance = new PortForwardedConnection( portForward )
    }

    def "Get Type"()
    {
        expect:
        instance.getType() == KubeConnectionType.PORT_FORWARDED
    }

    def "Get host name"()
    {
        given:
        InetAddress address = InetAddress.getLoopbackAddress(  )
        String expected = address.getHostName(  )

        1* portForward.getLocalAddress(  ) >> address

        when:
        String actual = instance.getHostName(  )

        then:
        actual == expected
    }

    def "Get port number"()
    {
        given:
        int expected = 9990

        1* portForward.getLocalPort(  ) >> expected

        when:
        int actual = instance.getPort(  )

        then:
        actual == expected
    }

    def "Close connection."()
    {
        when:
        instance.close(  )

        then:
        1* portForward.close(  )
    }

    def "Close connection fails, throws exception."()
    {
        given:
        1* portForward.close(  ) >> { throw new IOException( "Fake." )}

        when:
        instance.close(  )

        then:
        thrown ConnectionCloseException
    }
}
