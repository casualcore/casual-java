/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow

import io.netty.channel.Channel
import io.netty.channel.embedded.EmbeddedChannel
import jakarta.resource.spi.work.Work
import jakarta.resource.spi.work.WorkCompletedException
import jakarta.resource.spi.work.WorkEvent
import jakarta.resource.spi.work.WorkException
import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.buffer.type.ServiceBuffer
import se.laz.casual.api.external.json.JsonProvider
import se.laz.casual.api.external.json.JsonProviderFactory
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.TransactionState
import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.api.xa.XID
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import se.laz.casual.event.ServiceCallEventPublisher
import se.laz.casual.jca.SpanId
import se.laz.casual.jca.inbound.handler.service.ServiceHandler
import se.laz.casual.jca.inflow.work.CasualServiceCallWork
import se.laz.casual.network.CasualNWMessageDecoder
import se.laz.casual.network.CasualNWMessageEncoder
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.inbound.ProtocolVersionValueHolder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage
import spock.lang.Shared
import spock.lang.Specification

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
    @Shared ServiceCallEventPublisher serviceCallEventPublisher
    @Shared String serviceName = 'lovebites'
    @Shared UUID execution = UUID.randomUUID()
    @Shared Xid transactionId = createXid()
    @Shared CasualServiceCallRequestMessage request
    ProtocolVersionValueHolder valueHolder

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
                .setProtocolVersion(ProtocolVersion.VERSION_1_2)
                .build()
        response = CasualNWMessageImpl.of( correlationId, message )

        valueHolder = ProtocolVersionValueHolder.of()
        valueHolder.accept(ProtocolVersion.VERSION_1_2)
        inboundHandler = TestInboundHandler.of()
        channel = new EmbeddedChannel(CasualNWMessageDecoder.of(valueHolder), CasualNWMessageEncoder.of(), inboundHandler)
        work = new CasualServiceCallWork(correlationId, null, false, valueHolder.get(), SpanId.of())
        work.response = response

        request = CasualServiceCallRequestMessage.createBuilder()
               .setExecution(execution)
               .setParentName("")
               .setServiceName(serviceName)
               .setXid(transactionId)
                .setProtocolVersion(ProtocolVersion.VERSION_1_2)
               .build()
        instance = new ServiceCallWorkListener(channel, request, SpanId.of(), ProtocolVersion.VERSION_1_2)
        serviceCallEventPublisher = Mock(ServiceCallEventPublisher)
        instance.setEventPublisher(serviceCallEventPublisher)
    }

    def "WorkCompleted receives WorkEvent and writes work response to channel."()
    {
        setup:
        WorkEvent event = new WorkEvent( this, WorkEvent.WORK_COMPLETED, work, null )

        when:
        instance.workStarted(Mock(WorkEvent))
        instance.workCompleted( event )
        channel.writeInbound(channel.outboundMessages().element())
        CasualNWMessage<CasualServiceCallReplyMessage> actual = inboundHandler.getMsg()

        then:
        actual == response
        1 * serviceCallEventPublisher.post(_ as ServiceCallEvent) >> { ServiceCallEvent serviceCallEvent ->
           serviceCallEvent.getTransactionId() == transactionId
           serviceCallEvent.getExecution() == execution
           serviceCallEvent.getService() == serviceName
           serviceCallEvent.getCode() == ErrorState.OK.name()
           serviceCallEvent.getOrder() == Order.SEQUENTIAL.value
        }
    }

   def "TPNOREPLY, WorkCompleted receives WorkEvent and does not write work response to channel."()
   {
      setup:
      WorkEvent event = new WorkEvent( this, WorkEvent.WORK_COMPLETED, null, null )
      instance = new ServiceCallWorkListener(channel, request, true, SpanId.of(), ProtocolVersion.VERSION_1_2)
      instance.setEventPublisher(serviceCallEventPublisher)

      when:
      instance.workStarted(Mock(WorkEvent))
      instance.workCompleted( event )
      then:
      1 * serviceCallEventPublisher.post(_ as ServiceCallEvent) >> { ServiceCallEvent serviceCallEvent ->
         serviceCallEvent.getTransactionId() == transactionId
         serviceCallEvent.getExecution() == execution
         serviceCallEvent.getService() == serviceName
         serviceCallEvent.getCode() == ErrorState.OK.name()
         serviceCallEvent.getOrder() == Order.SEQUENTIAL.value
      }
      0 * channel.writeAndFlush(_ as CasualServiceCallReplyMessage)
   }

    def "TPNOREPLY, WorkCompleted correct ErrorState is reported in ServiceCallEvent"(Boolean failedUnexpectedly, ErrorState expectedErrorState)
    {
        setup:
        CasualServiceCallWork work = new CasualServiceCallWork(UUID.randomUUID(), request, true, ProtocolVersion.VERSION_1_2, SpanId.of())
        work.workFailedUnexpectedly = failedUnexpectedly
        WorkEvent event = new WorkEvent(this, WorkEvent.WORK_COMPLETED, work, null)
        instance = new ServiceCallWorkListener(channel, request, true, SpanId.of(), ProtocolVersion.VERSION_1_2)
        instance.setEventPublisher(serviceCallEventPublisher)
        String receivedErrorCode = null;

        when:
        instance.workStarted(Mock(WorkEvent))
        instance.workCompleted(event)

        then:
        1 * serviceCallEventPublisher.post(_ as ServiceCallEvent) >> { ServiceCallEvent serviceCallEvent ->
            receivedErrorCode = serviceCallEvent.getCode()
        }

        receivedErrorCode == expectedErrorState.name()

        where:
        failedUnexpectedly | expectedErrorState
        false              | ErrorState.OK
        true               | ErrorState.TPESYSTEM
    }

    Xid createXid()
    {
        String gid = Integer.toString( ThreadLocalRandom.current().nextInt() )
        String b = Integer.toString( ThreadLocalRandom.current().nextInt() )
        return XID.of(gid.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8), 0)
    }

    def "WorkCompleted with abnormal error, WorkEvent returns with an Exception and no reply"(ProtocolVersion protocolVersion)
    {
        given:
        def channel = Mock(Channel)
        def request = createRequestMessage(protocolVersion)

        def event = new WorkEvent(this, WorkEvent.WORK_COMPLETED, new CasualServiceCallWork(UUID.randomUUID(), request, false, protocolVersion, SpanId.of()), new WorkCompletedException("work failed unexpectedly", WorkException.INTERNAL))
        def workWithNullResponse = new CasualServiceCallWork(UUID.randomUUID(), request, false, protocolVersion, SpanId.of())
        def listener = new ServiceCallWorkListener(channel, request, SpanId.of(), protocolVersion)
        listener.setEventPublisher(serviceCallEventPublisher)
        CasualNWMessage written = null
        String receivedErrorCode = null

        when:
        listener.workStarted(event)
        listener.workCompleted(event)

        then:
        1 * channel.writeAndFlush(_) >> { args ->
            def msg = args[0]
            if (msg instanceof CasualNWMessage)
            {
                written = (CasualNWMessage) msg
            }
            return null
        }
        1 * serviceCallEventPublisher.post(_) >> { args ->
            ServiceCallEvent callEvent = (ServiceCallEvent) args[0]
            receivedErrorCode = callEvent.getCode()
        }
        noExceptionThrown()
        written != null
        written.getMessage() instanceof CasualServiceCallReplyMessage
        ErrorState.TPESYSTEM.name() == receivedErrorCode
        workWithNullResponse.getResponse() == null

        where:
        protocolVersion | _
        ProtocolVersion.VERSION_1_0 | _
        ProtocolVersion.VERSION_1_1 | _
        ProtocolVersion.VERSION_1_2 | _
        ProtocolVersion.VERSION_1_3 | _
        ProtocolVersion.VERSION_1_4 | _
    }

    def "WorkCompleted with normal casual error, WorkEvent returns without exception and with reply"(ProtocolVersion protocolVersion)
    {
        given:
        def channel = Mock(Channel)
        def request = createRequestMessage(protocolVersion)
        def replyBuilder = CasualServiceCallReplyMessage.createBuilder()
                .setExecution(request.getExecution())
                .setProtocolVersion(protocolVersion)
                .setServiceBuffer(null)
                .setError(ErrorState.TPESVCERR)
        if (protocolVersion.isLessThan(ProtocolVersion.VERSION_1_3))
        {
            replyBuilder.setXid(request.getXid())
        }
        def reply = replyBuilder.build()

        def workWithResponse = new CasualServiceCallWork(UUID.randomUUID(), request, false, protocolVersion, SpanId.of())
        workWithResponse.response = CasualNWMessageImpl.of(work.getCorrelationId(), reply)
        def event = new WorkEvent(this, WorkEvent.WORK_COMPLETED, workWithResponse, new WorkCompletedException("work failed unexpectedly", WorkException.INTERNAL))
        def listener = new ServiceCallWorkListener(channel, request, SpanId.of(), protocolVersion)
        listener.setEventPublisher(serviceCallEventPublisher)
        CasualNWMessage written = null
        String receivedErrorCode = null

        when:
        listener.workStarted(event)
        listener.workCompleted(event)

        then:
        workWithResponse.getResponse() != null
        1 * channel.writeAndFlush(_) >> { args ->
            def msg = args[0]
            if (msg instanceof CasualNWMessage)
            {
                written = (CasualNWMessage) msg
            }
            return null
        }
        1 * serviceCallEventPublisher.post(_) >> { args ->
            ServiceCallEvent callEvent = (ServiceCallEvent) args[0]
            receivedErrorCode = callEvent.getCode()
        }
        noExceptionThrown()
        written != null
        written.getMessage() instanceof CasualServiceCallReplyMessage
        ErrorState.TPESVCERR.name() == receivedErrorCode
        workWithResponse.getResponse() != null

        where:
        protocolVersion | _
        ProtocolVersion.VERSION_1_0 | _
        ProtocolVersion.VERSION_1_1 | _
        ProtocolVersion.VERSION_1_2 | _
        ProtocolVersion.VERSION_1_3 | _
        ProtocolVersion.VERSION_1_4 | _
    }

    private static CasualServiceCallRequestMessage createRequestMessage(ProtocolVersion protocolVersion)
    {
        return CasualServiceCallRequestMessage.createBuilder()
                .setXid(XID.NULL_XID)
                .setExecution(UUID.randomUUID())
                .setParentName("some-parent")
                .setServiceName("some-service")
                .setParentSpan(SpanId.of())
                .setProtocolVersion(protocolVersion)
                .build()
    }
}
