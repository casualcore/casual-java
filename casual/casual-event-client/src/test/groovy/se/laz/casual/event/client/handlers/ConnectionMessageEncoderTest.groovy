package se.laz.casual.event.client.handlers

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import se.laz.casual.event.client.messages.ConnectionMessage
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class ConnectionMessageEncoderTest extends Specification
{
    def 'encode'()
    {
        given:
        ChannelHandlerContext context = Mock(ChannelHandlerContext)
        ConnectionMessage message = ConnectionMessage.of()
        byte[] connectionMessageData = message.getConnectionMessage().getBytes(StandardCharsets.UTF_8)
        ByteBuf buffer = Mock(ByteBuf){
            1 * writeBytes(connectionMessageData)
        }
        ConnectionMessageEncoder encoder = ConnectionMessageEncoder.of()
        when:
        encoder.encode(context, message, buffer)
        then:
        noExceptionThrown()
    }

    def 'only accepts ConnectionMessage'()
    {
        given:
        ConnectionMessageEncoder encoder = ConnectionMessageEncoder.of()
        expect:
        encoder.acceptOutboundMessage(data) == expected
        where:
        data                       ||    expected
        ConnectionMessage.of()     ||    true
        'not a connection message' ||    false
    }

}
