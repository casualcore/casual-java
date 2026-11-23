/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import se.laz.casual.network.connection.CasualConnectionException
import spock.lang.Specification

class ErrorInformerTest extends Specification
{
    def 'inform notifies registered listeners once'()
    {
        given:
        CasualConnectionException cause = new CasualConnectionException('connection lost')
        ErrorInformer informer = ErrorInformer.of(cause)
        NetworkListener listener = Mock(NetworkListener)
        informer.addListener(listener)

        when:
        informer.inform()
        informer.inform()

        then:
        1 * listener.disconnected(cause)
    }

    def 'listener added after inform is notified immediately'()
    {
        given:
        CasualConnectionException cause = new CasualConnectionException('closed before registration')
        ErrorInformer informer = ErrorInformer.of(cause)
        NetworkListener lateListener = Mock(NetworkListener)
        informer.inform()

        when:
        informer.addListener(lateListener)

        then:
        1 * lateListener.disconnected(cause)
    }

    def 'null listener is rejected'()
    {
        given:
        ErrorInformer informer = ErrorInformer.of(new CasualConnectionException('boom'))

        when:
        informer.addListener(null)

        then:
        thrown(NullPointerException)
    }
}
