/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;

import io.netty.channel.Channel;
import jakarta.resource.spi.work.Work;
import jakarta.resource.spi.work.WorkEvent;
import jakarta.resource.spi.work.WorkListener;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.config.ConfigurationOptions;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.event.Order;
import se.laz.casual.event.ServiceCallEvent;
import se.laz.casual.event.ServiceCallEventPublisher;
import se.laz.casual.event.ServiceCallEventStoreFactory;
import se.laz.casual.jca.inflow.work.CasualServiceCallWork;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;

import java.util.UUID;

/**
 * Work Listener to handle completion of {@link jakarta.resource.spi.work.Work} item by
 * {@link jakarta.resource.spi.work.WorkManager} to flush the response to the netty {@link Channel}.
 */
public class ServiceCallWorkListener implements WorkListener
{
    private final Channel channel;
    private final boolean isTpNoReply;
    private final CasualServiceCallRequestMessage message;
    private ServiceCallEventPublisher eventPublisher;

    private final ServiceCallEvent.Builder eventBuilder;

    public ServiceCallWorkListener(Channel channel, CasualServiceCallRequestMessage message)
    {
        this(channel, message, false);
    }

    public ServiceCallWorkListener(Channel channel, CasualServiceCallRequestMessage message, boolean isTpNoReply)
    {
        this.channel = channel;
        this.message = message;
        this.isTpNoReply = isTpNoReply;
        this.eventBuilder = ServiceCallEvent.createBuilder();
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
        eventBuilder.start();
    }

    @Override
    public void workCompleted(WorkEvent e)
    {
        eventBuilder.end();
        ServiceCallEvent event = createEvent( e.getWork() );
        getEventPublisher().post(event);
        if(!isTpNoReply)
        {
            CasualServiceCallWork work = (CasualServiceCallWork) e.getWork();
            channel.writeAndFlush(work.getResponse());
        }
    }

    private ServiceCallEvent createEvent(Work work )
    {
        eventBuilder.withTransactionId(message.getXid())
                   .withExecution(message.getExecution())
                   .withParent(message.getParentName())
                   .withService(message.getServiceName())
                   .withOrder(Order.SEQUENTIAL);
        if(!isTpNoReply && work instanceof CasualServiceCallWork casualWork)
        {
            CasualServiceCallReplyMessage reply = casualWork.getResponse().getMessage();
            eventBuilder.withCode(reply.getError());
        }
        else
        {
            eventBuilder.withCode(ErrorState.OK);
        }
        return eventBuilder.build();
    }

    ServiceCallEventPublisher getEventPublisher()
    {
        if(eventPublisher == null)
        {
            UUID domainId = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_DOMAIN_ID );
            eventPublisher = ServiceCallEventPublisher.of(ServiceCallEventStoreFactory.getStore(domainId));
        }
        return eventPublisher;
    }

    void setEventPublisher(ServiceCallEventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

}
