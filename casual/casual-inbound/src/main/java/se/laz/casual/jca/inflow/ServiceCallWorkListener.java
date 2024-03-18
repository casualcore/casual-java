/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;

import io.netty.channel.Channel;
import jakarta.resource.spi.work.Work;
import jakarta.resource.spi.work.WorkEvent;
import jakarta.resource.spi.work.WorkListener;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.event.Order;
import se.laz.casual.event.ServiceCallEvent;
import se.laz.casual.event.ServiceCallEventHandlerFactory;
import se.laz.casual.event.ServiceCallEventPublisher;
import se.laz.casual.jca.inflow.work.CasualServiceCallWork;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Work Listener to handle completion of {@link jakarta.resource.spi.work.Work} item by
 * {@link jakarta.resource.spi.work.WorkManager} to flush the response to the netty {@link Channel}.
 */
public class ServiceCallWorkListener implements WorkListener
{
    private final Channel channel;
    private final Instant creationTime = Instant.now();
    private final boolean isTpNoReply;
    private final CasualServiceCallRequestMessage message;
    private Instant sometimeBeforeServiceCall;
    private ServiceCallEventPublisher eventPublisher;

    public ServiceCallWorkListener(Channel channel, CasualServiceCallRequestMessage message)
    {
        this(channel, message, false);
    }

    public ServiceCallWorkListener(Channel channel, CasualServiceCallRequestMessage message, boolean isTpNoReply)
    {
        this.channel = channel;
        this.message = message;
        this.isTpNoReply = isTpNoReply;
    }

    @Override
    public void workAccepted(WorkEvent e)
    {
        //No Op
    }

    @Override
    public void workRejected(WorkEvent e)
    {
        // No Op
    }

    @Override
    public void workStarted(WorkEvent e)
    {
        sometimeBeforeServiceCall = Instant.now();
    }

    @Override
    public void workCompleted(WorkEvent e)
    {
        Instant sometimeAfterServiceCall = Instant.now();
        ServiceCallEvent event = createEvent(e.getWork(), sometimeAfterServiceCall);
        getEventPublisher().post(event);
        if(!isTpNoReply)
        {
            CasualServiceCallWork work = (CasualServiceCallWork) e.getWork();
            channel.writeAndFlush(work.getResponse());
        }
    }

    private ServiceCallEvent createEvent(Work work, Instant sometimeAfterServiceCall)
    {
        long pending = ChronoUnit.MICROS.between(creationTime, sometimeBeforeServiceCall);
        ServiceCallEvent.Builder builder = ServiceCallEvent.createBuilder()
                                                           .withTransactionId(message.getXid())
                                                           .withExecution(message.getExecution())
                                                           .withParent(message.getParentName())
                                                           .withPending(pending)
                                                           .withService(message.getServiceName())
                                                           .withStart(sometimeBeforeServiceCall)
                                                           .withEnd(sometimeAfterServiceCall)
                                                           .withOrder(Order.SEQUENTIAL);
        if(!isTpNoReply && work instanceof CasualServiceCallWork casualWork)
        {
            CasualServiceCallReplyMessage reply = casualWork.getResponse().getMessage();
            builder.withCode(reply.getError());
        }
        else
        {
            builder.withCode(ErrorState.OK);
        }
        return builder.build();
    }

    ServiceCallEventPublisher getEventPublisher()
    {
        if(eventPublisher == null)
        {
            eventPublisher = ServiceCallEventPublisher.of(ServiceCallEventHandlerFactory.getHandler());
        }
        return eventPublisher;
    }

    void setEventPublisher(ServiceCallEventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

}
