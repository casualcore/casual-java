package se.laz.casual.event.client.handlers

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.util.CharsetUtil
import se.laz.casual.api.external.json.JsonProviderFactory
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.xa.XID
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import se.laz.casual.event.client.EventObserver
import spock.lang.Shared
import spock.lang.Specification

import javax.transaction.xa.Xid
import java.time.Instant

class FromJSONEventMessageDecoderTest extends Specification
{
    @Shared
    Xid transactionId = XID.NULL_XID
    def 'normal flow'()
    {
        given:
        ServiceCallEvent event = createEvent()
        EventObserver observer = Mock(EventObserver){
            1 * notify(event)
        }
        ChannelHandlerContext context = Mock(ChannelHandlerContext)
        ByteBuf buffer = Mock(ByteBuf){
            1 * toString(CharsetUtil.UTF_8) >> {
                JsonProviderFactory.getJsonProvider().toJson(event)
            }
        }
        FromJSONEventMessageDecoder decoder = FromJSONEventMessageDecoder.of(observer)
        when:
        decoder.channelRead0(context, buffer)
        then:
        noExceptionThrown()
    }

    ServiceCallEvent createEvent()
    {
        return ServiceCallEvent.createBuilder()
                .withCode(ErrorState.OK)
                .withStart(Instant.now())
                .withEnd(Instant.now())
                .withExecution(UUID.randomUUID())
                .withOrder(Order.CONCURRENT)
                .withService("fast-service")
                .withPID(42)
                .withTransactionId(transactionId)
                .build()
    }
}
