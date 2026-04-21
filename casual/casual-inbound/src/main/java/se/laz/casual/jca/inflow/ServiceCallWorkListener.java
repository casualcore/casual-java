/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;

import io.netty.channel.Channel;
import jakarta.resource.spi.work.WorkEvent;
import jakarta.resource.spi.work.WorkException;
import jakarta.resource.spi.work.WorkListener;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.TransactionState;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.config.ConfigurationOptions;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.event.Order;
import se.laz.casual.event.ServiceCallEvent;
import se.laz.casual.event.ServiceCallEventPublisher;
import se.laz.casual.event.ServiceCallEventStoreFactory;
import se.laz.casual.jca.SpanId;
import se.laz.casual.jca.CasualResourceAdapterException;
import se.laz.casual.jca.inflow.work.CasualServiceCallWork;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static se.laz.casual.network.ProtocolVersion.VERSION_1_3;

/**
 * Work Listener to handle completion of {@link jakarta.resource.spi.work.Work} item by
 * {@link jakarta.resource.spi.work.WorkManager} to flush the response to the netty {@link Channel}.
 */
public class ServiceCallWorkListener implements WorkListener
{
    private static final Logger log = Logger.getLogger(ServiceCallWorkListener.class.getName());
    private final Channel channel;
    private final boolean isTpNoReply;
    private final CasualServiceCallRequestMessage message;
    private final ProtocolVersion protocolVersion;
    private ServiceCallEventPublisher eventPublisher;
    private boolean replySent = false;

    private final ServiceCallEvent.Builder eventBuilder;
    private final SpanId spanId;

    public ServiceCallWorkListener(Channel channel, CasualServiceCallRequestMessage message, SpanId spanId, ProtocolVersion protocolVersion)
    {
        this(channel, message, false, spanId, protocolVersion);
    }

    public ServiceCallWorkListener(Channel channel, CasualServiceCallRequestMessage message, boolean isTpNoReply, SpanId spanId, ProtocolVersion protocolVersion)
    {
        this.channel = channel;
        this.message = message;
        this.isTpNoReply = isTpNoReply;
        this.eventBuilder = ServiceCallEvent.createBuilder();
        this.spanId = spanId;
        this.protocolVersion = protocolVersion;
    }

    @Override
    public void workAccepted(WorkEvent e)
    {
        log.finest(() -> "Accepted CasualServiceCallWork with corrid=%s".formatted(getCasualCorrelationId(getCasualServiceCallWork(e))));
    }

    @Override
    public void workRejected(WorkEvent e)
    {
        CasualServiceCallWork work = getCasualServiceCallWork(e);
        if (log.isLoggable(Level.WARNING))
        {
            if (null != e.getException())
            {
                log.log(Level.WARNING,
                        "CasualServiceCallWork rejected with errorCode=%s, corrid=%s".formatted(
                                getDescriptiveWorkExceptionErrorCode(e.getException()),
                                getCasualCorrelationId(work)),
                        e.getException());
            }
            else
            {
                log.log(Level.WARNING,
                        "CasualServiceCallWork rejected without an exception, this is not normal, corrid=%s".formatted(getCasualCorrelationId(work)));
            }
        }

        if (!isTpNoReply)
        {
            sendReply(createEmptyErrorBuffer(work));
        }
    }

    @Override
    public void workStarted(WorkEvent e)
    {
        log.finest(() -> "Work started in %d ms with corrid=%s".formatted(e.getStartDuration(), getCasualCorrelationId(getCasualServiceCallWork(e))));
        eventBuilder.start();
    }

    @Override
    public void workCompleted(WorkEvent e)
    {
        log.finest(() -> "Work completed %s exception, corrid=%s".formatted(e.getException() == null ? "without" : "with", getCasualCorrelationId(getCasualServiceCallWork(e))));
        eventBuilder.end();
        if (e.getException() != null)
        {
            log.log(Level.SEVERE, "Inbound call failed with an exception, errorCode=%s".formatted(getDescriptiveWorkExceptionErrorCode(e.getException())), e.getException());
        }
        CasualServiceCallWork work = getCasualServiceCallWork(e);
        ServiceCallEvent event = createEvent(work);
        getEventPublisher().post(event);
        if(!isTpNoReply)
        {
            if(workContainsReply(work))
            {
                sendReply(work.getResponse());
            }
            else
            {
                sendReply(createEmptyErrorBuffer(work));
            }
        }
    }

    private ServiceCallEvent createEvent(CasualServiceCallWork work)
    {
        eventBuilder.withTransactionId(message.getXid())
                    .withExecution(message.getExecution())
                    .withParent(message.getParentName())
                    .withService(message.getServiceName())
                    .withOrder(Order.SEQUENTIAL);
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ))
        {
            eventBuilder.withParentSpanId(message.getParentSpan().asHex())
                        .withSpanId(spanId.asHex());
            if(!isTpNoReply && workContainsReply(work))
            {
                CasualServiceCallReplyMessage reply = work.getResponse().getMessage();

                eventBuilder.withUserCode(reply.getUserDefinedCode());
            }
        }
        if(!isTpNoReply)
        {
            eventBuilder.withCode(
                    workContainsReply(work)
                    ? work.getResponse().getMessage().getError()
                    : ErrorState.TPESYSTEM
            );
        }
        else
        {
            eventBuilder.withCode(
                    work != null && !work.failedUnexpectedly()
                    ? ErrorState.OK
                    : ErrorState.TPESYSTEM
            );
        }
        return eventBuilder.build();
    }

    ServiceCallEventPublisher getEventPublisher()
    {
        if(eventPublisher == null)
        {
            UUID domainId = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_DOMAIN_ID ).getId();
            eventPublisher = ServiceCallEventPublisher.of(ServiceCallEventStoreFactory.getStore(domainId));
        }
        return eventPublisher;
    }

    void setEventPublisher(ServiceCallEventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    private boolean workContainsReply(CasualServiceCallWork work)
    {
        return work.getResponse() != null && work.getResponse().getMessage() != null;
    }

    private CasualServiceCallWork getCasualServiceCallWork(WorkEvent e)
    {
        return (CasualServiceCallWork) e.getWork();
    }

    private String getDescriptiveWorkExceptionErrorCode(WorkException e)
    {
        return e.getErrorCode() == null
                ? "null"
                : switch (e.getErrorCode())
        {
            case WorkException.INTERNAL -> "INTERNAL(-1)";
            case WorkException.UNDEFINED -> "UNDEFINED(0)";
            case WorkException.START_TIMED_OUT -> "START_TIMED_OUT(1)";
            case WorkException.TX_CONCURRENT_WORK_DISALLOWED -> "TX_CONCURRENT_WORK_DISALLOWED(2)";
            case WorkException.TX_RECREATE_FAILED -> "TX_RECREATE_FAILED(3)";
            default -> "UNKNOWN(%s)".formatted(e.getErrorCode());
        };
    }

    private UUID getCasualCorrelationId(CasualServiceCallWork work)
    {
        if (work.getCorrelationId() != null)
        {
            return work.getCorrelationId();
        }
        else
        {
            throw new CasualResourceAdapterException("Casual Work does not contain a correlation id, this should not happen.");
        }
    }

    private CasualNWMessage<?> createEmptyErrorBuffer(CasualServiceCallWork work)
    {
        CasualServiceCallReplyMessage.Builder replyMessageBuilder = new CasualServiceCallReplyMessage.Builder()
                .setProtocolVersion(protocolVersion)
                .setExecution(work.getMessage().getExecution())
                .setError(ErrorState.TPESYSTEM)
                .setTransactionState(TransactionState.ROLLBACK_ONLY)
                .setServiceBuffer(ServiceBuffer.nullBuffer());

        if (protocolVersion.isLessThan( VERSION_1_3) )
        {
            replyMessageBuilder.setXid(work.getMessage().getXid());
        }

        return CasualNWMessageImpl.of(work.getCorrelationId(), replyMessageBuilder.build());
    }

    private void sendReply(CasualNWMessage<?> replyMessage)
    {
        if (!replySent)
        {
            channel.writeAndFlush(replyMessage);
            replySent = true;
        }
        else
        {
            throw new RuntimeException("Response already sent to casual, there seems to be a logic error");
        }
    }

}
