package se.kodarkatten.casual.jca.inflow

import io.netty.channel.embedded.EmbeddedChannel
import se.kodarkatten.casual.api.buffer.CasualBuffer
import se.kodarkatten.casual.api.buffer.type.JsonBuffer
import se.kodarkatten.casual.api.external.json.JsonProvider
import se.kodarkatten.casual.api.external.json.JsonProviderFactory
import se.kodarkatten.casual.api.flags.ErrorState
import se.kodarkatten.casual.api.flags.TransactionState
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.jca.inbound.handler.service.ServiceHandler
import se.kodarkatten.casual.jca.inflow.work.CasualServiceCallWork
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.messages.service.CasualServiceCallReplyMessage
import se.kodarkatten.casual.network.protocol.messages.service.ServiceBuffer
import se.laz.casual.network.CasualNWMessageDecoder
import se.laz.casual.network.CasualNWMessageEncoder
import spock.lang.Shared
import spock.lang.Specification

import javax.resource.spi.work.WorkEvent
import javax.transaction.xa.Xid
import java.nio.charset.StandardCharsets
import java.util.concurrent.ThreadLocalRandom

class ServiceCallWorkListenerTest extends Specification
{
    @Shared ServiceCallWorkListener instance
    @Shared CasualServiceCallWork work
    @Shared EmbeddedChannel channel
    @Shared UUID correlationId

    @Shared String methodParam = "method param1"
    @Shared JsonProvider jp = JsonProviderFactory.getJsonProvider()

    @Shared ServiceHandler handler
    @Shared List<byte[]> payload
    @Shared CasualBuffer buffer
    @Shared CasualNWMessage<CasualServiceCallReplyMessage> response
    @Shared TestInboundHandler inboundHandler

    def setup()
    {
        handler = Mock(ServiceHandler)
        payload = new ArrayList<>()
        byte[] p = jp.toJson(methodParam).getBytes(StandardCharsets.UTF_8)
        payload.add(p)
        buffer = JsonBuffer.of(payload)

        correlationId = UUID.randomUUID()

        CasualServiceCallReplyMessage message = CasualServiceCallReplyMessage.createBuilder()
                .setXid( createXid() )
                .setError( ErrorState.OK )
                .setTransactionState( TransactionState.TX_ACTIVE )
                .setExecution( correlationId )
                .setServiceBuffer( ServiceBuffer.of( buffer ) )
                .build()
        response = CasualNWMessageImpl.of( correlationId, message )


        inboundHandler = TestInboundHandler.of()
        channel = new EmbeddedChannel(CasualNWMessageDecoder.of(), CasualNWMessageEncoder.of(), inboundHandler)
        work = new CasualServiceCallWork( correlationId, null )
        work.response = response

        instance = new ServiceCallWorkListener( channel )
    }

    def "WorkCompleted receives WorkEvent and writes work response to channel."()
    {
        setup:
        WorkEvent event = new WorkEvent( this, WorkEvent.WORK_COMPLETED, work, null )

        when:
        instance.workCompleted( event )
        channel.writeInbound(channel.outboundMessages().element())
        CasualNWMessage<CasualServiceCallReplyMessage> actual = inboundHandler.getMsg()

        then:
        actual == response
    }

    Xid createXid()
    {
        String gid = Integer.toString( ThreadLocalRandom.current().nextInt() )
        String b = Integer.toString( ThreadLocalRandom.current().nextInt() )
        return XID.of(gid.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8), 0)
    }
}
