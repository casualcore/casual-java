package se.laz.casual.event.client.handlers

import io.netty.channel.ChannelHandlerContext
import spock.lang.Specification

class ExceptionHandlerTest extends Specification
{
    def 'exceptional'()
    {
        given:
        ChannelHandlerContext context = Mock(ChannelHandlerContext){
            1 * close()
        }
        Throwable cause = Mock(Throwable)
        ExceptionHandler handler = ExceptionHandler.of()
        when:
        handler.exceptionCaught(context, cause)
        then:
        noExceptionThrown()
    }
}
