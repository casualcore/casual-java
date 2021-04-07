/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow

import com.google.protobuf.ByteString
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender
import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.external.json.JsonProvider
import se.laz.casual.api.external.json.JsonProviderFactory
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.TransactionState
import se.laz.casual.api.xa.XID
import se.laz.casual.jca.inbound.handler.service.ServiceHandler
import se.laz.casual.jca.inflow.work.CasualServiceCallWork
import se.laz.casual.network.grpc.MessageCreator
import se.laz.casual.network.messages.CasualReply
import se.laz.casual.network.messages.CasualServiceCallReply
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
    @Shared CasualReply response
    @Shared TestInboundHandler inboundHandler

    def setup()
    {
        handler = Mock(ServiceHandler)
        payload = new ArrayList<>()
        byte[] p = jp.toJson(methodParam).getBytes(StandardCharsets.UTF_8)
        payload.add(p)
        buffer = JsonBuffer.of(payload)

        correlationId = UUID.randomUUID()

        CasualServiceCallReply message = CasualServiceCallReply.newBuilder()
                .setXid(MessageCreator.toXID(createXid()))
                .setResult(MessageCreator.toErrorState(ErrorState.OK))
                .setTransactionState(MessageCreator.toTransactionState(TransactionState.TX_ACTIVE))
                .setExecution(MessageCreator.toUUID4(correlationId))
                .setPayload(ByteString.copyFrom(p))
                .setBufferTypeName('json')
                .build()

        response = CasualReply.newBuilder()
                .setMessageType(CasualReply.MessageType.SERVICE_CALL_REPLY)
                .setCorrelationId(MessageCreator.toUUID4(correlationId))
                .setServiceCall(message)
                .build()


        inboundHandler = TestInboundHandler.of()
        channel = new EmbeddedChannel(new ProtobufVarint32FrameDecoder(), new ProtobufDecoder(CasualReply.getDefaultInstance()),
                new ProtobufVarint32LengthFieldPrepender(), new ProtobufEncoder(), inboundHandler)
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
        CasualReply actualEnvelope = inboundHandler.getMsg()

        then:
        actualEnvelope == response
    }

    Xid createXid()
    {
        String gid = Integer.toString( ThreadLocalRandom.current().nextInt() )
        String b = Integer.toString( ThreadLocalRandom.current().nextInt() )
        return XID.of(gid.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8), 0)
    }
}
