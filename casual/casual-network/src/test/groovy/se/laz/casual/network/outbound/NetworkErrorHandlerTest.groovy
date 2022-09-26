package se.laz.casual.network.outbound

import io.netty.channel.Channel
import spock.lang.Specification

class NetworkErrorHandlerTest extends Specification
{
    def 'disconnect if channel !active'()
    {
        given:
        def channel = Mock(Channel)
        1 * channel.isActive() >> {
            false
        }
        def networkListener = Mock(NetworkListener)
        when:
        NetworkErrorHandler.notifyListenersIfNotConnected(channel, [networkListener])
        then:
        1 * networkListener.disconnected(_)
    }

    def 'do nothing if channel active'()
    {
        given:
        def channel = Mock(Channel)
        1 * channel.isActive() >> {
            true
        }
        def networkListener = Mock(NetworkListener)
        when:
        NetworkErrorHandler.notifyListenersIfNotConnected(channel, [networkListener])
        then:
        0 * networkListener.disconnected(_)
    }
}
