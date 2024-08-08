/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;

import io.netty.channel.Channel;
import jakarta.resource.spi.work.WorkEvent;
import jakarta.resource.spi.work.WorkListener;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.event.ServiceCallEvent;
import se.laz.casual.event.ServiceCallEventPublisher;
import se.laz.casual.event.ServiceCallEventStoreFactory;

import java.util.UUID;

/**
 * Work Listener to handle completion of {@link jakarta.resource.spi.work.Work} item by
 * {@link jakarta.resource.spi.work.WorkManager} to flush the response to the netty {@link Channel}.
 */
public class ServiceCallWorkListener implements WorkListener
{
    private final Channel channel;
    private final boolean isTpNoReply;
    private final WorkResponseContext context;
    private final ResponseExtractFunction extractFunction;
    private ServiceCallEventPublisher eventPublisher;

    private final ServiceCallEvent.Builder eventBuilder;

    public ServiceCallWorkListener(Channel channel, WorkResponseContext context)
    {
        this(channel, context, false, ResponseCreator::create);
    }

    public ServiceCallWorkListener(Channel channel, WorkResponseContext context, boolean isTpNoReply, ResponseExtractFunction extractFunction)
    {
        this.channel = channel;
        this.context = context;
        this.isTpNoReply = isTpNoReply;
        this.eventBuilder = ServiceCallEvent.createBuilder();
        this.extractFunction = extractFunction;
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
    public void workCompleted(WorkEvent workEvent)
    {
        eventBuilder.end();
        ServiceCallResult result = extractFunction.extract(workEvent, context, isTpNoReply);
        ServiceCallEvent event = ServiceCallEventCreator.createEvent( eventBuilder, context, result.resultCode());
        getEventPublisher().post(event);
        if(!isTpNoReply)
        {
            result.maybeResult().ifPresent(channel::writeAndFlush);
        }
    }

    ServiceCallEventPublisher getEventPublisher()
    {
        if(eventPublisher == null)
        {
            UUID domainId = ConfigurationService.getInstance().getConfiguration().getDomain().getId();
            eventPublisher = ServiceCallEventPublisher.of(ServiceCallEventStoreFactory.getStore(domainId));
        }
        return eventPublisher;
    }

    void setEventPublisher(ServiceCallEventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

}
