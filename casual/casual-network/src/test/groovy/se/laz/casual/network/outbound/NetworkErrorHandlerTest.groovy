package se.laz.casual.network.outbound

import io.netty.channel.Channel
import se.laz.casual.network.connection.CasualConnectionException
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
        ErrorInformer errorInformer = ErrorInformer.of(new CasualConnectionException("connection gone"))
        errorInformer.addListener(networkListener)
        when:
        NetworkErrorHandler.notifyListenersIfNotConnected(channel, errorInformer)
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
        ErrorInformer errorInformer = ErrorInformer.of(new CasualConnectionException("connection gone"))
        errorInformer.addListener(networkListener)
        when:
        NetworkErrorHandler.notifyListenersIfNotConnected(channel, errorInformer)
        then:
        0 * networkListener.disconnected(_)
    }
}
