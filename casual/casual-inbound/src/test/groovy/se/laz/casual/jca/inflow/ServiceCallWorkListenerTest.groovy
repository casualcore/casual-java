/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow

import io.netty.channel.embedded.EmbeddedChannel
import jakarta.resource.spi.work.WorkEvent
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
import se.laz.casual.jca.inbound.handler.service.ServiceHandler
import se.laz.casual.jca.inflow.work.CasualServiceCallWork
import se.laz.casual.network.CasualNWMessageDecoder
import se.laz.casual.network.CasualNWMessageEncoder
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
        work = new CasualServiceCallWork(correlationId, null)
        work.response = response

        request = CasualServiceCallRequestMessage.createBuilder()
               .setExecution(execution)
               .setParentName("")
               .setServiceName(serviceName)
               .setXid(transactionId)
               .build()
        instance = new ServiceCallWorkListener(channel, request)
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
      instance = new ServiceCallWorkListener(channel, request, true)
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

    Xid createXid()
    {
        String gid = Integer.toString( ThreadLocalRandom.current().nextInt() )
        String b = Integer.toString( ThreadLocalRandom.current().nextInt() )
        return XID.of(gid.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8), 0)
    }
}
