/*
 * Copyright (c) 2017 - 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.service;

import se.laz.casual.api.CasualServiceApi;
import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.ServiceReturnState;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;
import se.laz.casual.api.service.ServiceDetails;
import se.laz.casual.api.util.PrettyPrinter;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.config.Domain;
import se.laz.casual.event.Order;
import se.laz.casual.event.ServiceCallEventHandlerFactory;
import se.laz.casual.event.ServiceCallEventPublisher;
import se.laz.casual.jca.CasualManagedConnection;
import se.laz.casual.network.connection.CasualConnectionException;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.laz.casual.network.protocol.messages.domain.Service;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;

import javax.transaction.xa.Xid;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class CasualServiceCaller implements CasualServiceApi
{
    private static final Logger LOG = Logger.getLogger(CasualServiceCaller.class.getName());
    private static final String SERVICE_NAME_LITERAL = " serviceName: ";
    private ServiceCallEventPublisher  eventPublisher;
    private final CasualManagedConnection connection;

    private CasualServiceCaller(CasualManagedConnection connection)
    {
        this.connection = connection;
    }

    public static CasualServiceCaller of(CasualManagedConnection connection)
    {
        return new CasualServiceCaller(connection);
    }

    @Override
    public ServiceReturn<CasualBuffer> tpcall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        try
        {
            throwIfTpCallFlagsInvalid(serviceName, flags);
            return issueAsyncCall(serviceName, data, flags).join().orElseThrow(() -> new CasualConnectionException("result is missing, it should always be returned"));
        }
        catch (Exception e)
        {
            throw new CasualConnectionException(e);
        }
    }

    @Override
    public CompletableFuture<Optional<ServiceReturn<CasualBuffer>>> tpacall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        throwIfTpacallFlagsInvalid(serviceName, flags);
        return issueAsyncCall(serviceName, data, flags);
    }

    private CompletableFuture<Optional<ServiceReturn<CasualBuffer>>> issueAsyncCall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        CompletableFuture<Optional<ServiceReturn<CasualBuffer>>> f = new CompletableFuture<>();
        UUID corrId = UUID.randomUUID();
        boolean noReply = flags.isSet(AtmiFlags.TPNOREPLY);
        final UUID execution = UUID.randomUUID();
        final Instant start = Instant.now();
        final Xid xid = connection.getCurrentXid();
        Optional<CompletableFuture<CasualNWMessage<CasualServiceCallReplyMessage>>> maybeServiceReturnValue = makeServiceCall(corrId, serviceName, data, flags, xid, execution, noReply);
        maybeServiceReturnValue.ifPresent(casualNWMessageCompletableFuture ->
                casualNWMessageCompletableFuture.whenComplete((v, e) -> {
                            if (null != e)
                            {
                                LOG.finest(() -> "service call request failed for corrid: " + PrettyPrinter.casualStringify(corrId) + SERVICE_NAME_LITERAL + serviceName);
                                f.completeExceptionally(e);
                                return;
                            }
                            LOG.finest(() -> "service call request ok for corrid: " + PrettyPrinter.casualStringify(corrId) + SERVICE_NAME_LITERAL + serviceName);
                            final Instant end = Instant.now();
                            getEventPublisher().createAndPostEvent( xid, execution, "", serviceName, v.getMessage().getError(), 0, start, end, Order.SEQUENTIAL);
                    if(!f.isDone())
                    {
                        f.complete(Optional.of(toServiceReturn(v)));
                    }
                }));
        if(noReply)
        {
            final Instant end = Instant.now();
            getEventPublisher().createAndPostEvent( xid, execution, "", serviceName, ErrorState.OK, 0, start, end, Order.SEQUENTIAL);
            f.complete(Optional.empty());
        }
        return f;
    }

    private void throwIfTpCallFlagsInvalid(String serviceName, Flag<AtmiFlags> flags)
    {
        if(flags.isSet(AtmiFlags.TPNOREPLY))
        {
            throw new CasualProtocolException("tpCall to: + " + serviceName + " with TPNOREPLY - not allowed, should use tpacall");
        }
    }

    private void throwIfTpacallFlagsInvalid(String serviceName, Flag<AtmiFlags> flags)
    {
        if(flags.isSet(AtmiFlags.TPNOREPLY) && !flags.isSet(AtmiFlags.TPNOTRAN))
        {
            throw new CasualProtocolException("tpacall to: " + serviceName + " with TPNOREPLY but missing TPNOTRAN - not allowed");
        }
    }

    @Override
    public boolean serviceExists(String serviceName)
    {
        try
        {
            return serviceExists(UUID.randomUUID(), serviceName);
        }
        catch (Exception e)
        {
            throw new CasualConnectionException(e);
        }
    }

    @Override
    public List<ServiceDetails> serviceDetails(String serviceName)
    {
        List<ServiceDetails> serviceDetailsList = new ArrayList<>();

        CasualNWMessage<CasualDomainDiscoveryReplyMessage> replyMsg = serviceDiscovery(UUID.randomUUID(), serviceName);
        replyMsg
                .getMessage()
                .getServices()
                .forEach(service -> serviceDetailsList.add(
                        ServiceDetails.createBuilder()
                                .withName(service.getName())
                                .withCategory(service.getCategory())
                                .withTransactionType(service.getTransactionType())
                                .withTimeout(service.getTimeout())
                                .withHops(service.getHops()).build()));

        return serviceDetailsList;
    }

    @Override
    public String toString()
    {
        return "CasualServiceCaller{" +
                "connection=" + connection +
                '}';
    }

    private Optional<CompletableFuture<CasualNWMessage<CasualServiceCallReplyMessage>>> makeServiceCall(UUID corrid, String serviceName, CasualBuffer data, Flag<AtmiFlags> flags, Xid transactionId, UUID execution, boolean noReply)
    {
        Duration timeout = Duration.of(connection.getTransactionTimeout(), ChronoUnit.SECONDS);
        CasualServiceCallRequestMessage serviceRequestMessage = CasualServiceCallRequestMessage.createBuilder()
                .setExecution(execution)
                .setServiceBuffer(ServiceBuffer.of(data))
                .setServiceName(serviceName)
                .setXid(transactionId)
                .setTimeout(timeout.toNanos())
                .setXatmiFlags(flags).build();
        CasualNWMessage<CasualServiceCallRequestMessage> serviceRequestNetworkMessage = CasualNWMessageImpl.of(corrid, serviceRequestMessage);
        LOG.finest(() -> "issuing service call request, corrid: " + PrettyPrinter.casualStringify(corrid) + SERVICE_NAME_LITERAL + serviceName);

        if(noReply)
        {
            connection.getNetworkConnection().requestNoReply(serviceRequestNetworkMessage);
            return Optional.empty();
        }
        return Optional.of(connection.getNetworkConnection().request(serviceRequestNetworkMessage));
    }

    private CasualNWMessage<CasualDomainDiscoveryReplyMessage> serviceDiscovery(UUID corrid, String serviceName)
    {
        LOG.finest(() -> "issuing domain discovery, corrid: " + PrettyPrinter.casualStringify(corrid) + SERVICE_NAME_LITERAL + serviceName);
        Domain domain = ConfigurationService.getInstance().getConfiguration().getDomain();
        CasualDomainDiscoveryRequestMessage requestMsg = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(UUID.randomUUID())
                .setDomainId(domain.getId())
                .setDomainName(domain.getName())
                .setServiceNames(Arrays.asList(serviceName))
                .build();
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> msg = CasualNWMessageImpl.of(corrid, requestMsg);
        CompletableFuture<CasualNWMessage<CasualDomainDiscoveryReplyMessage>> replyMsgFuture = connection.getNetworkConnection().request(msg);

        CasualNWMessage<CasualDomainDiscoveryReplyMessage> replyMsg = replyMsgFuture.join();
        LOG.finest(() -> "domain discovery ok for corrid: " + PrettyPrinter.casualStringify(corrid) + SERVICE_NAME_LITERAL + serviceName);
        return replyMsg;
    }

    private boolean serviceExists(UUID corrid, String serviceName)
    {
        CasualNWMessage<CasualDomainDiscoveryReplyMessage> replyMsg = serviceDiscovery(corrid, serviceName);
        return replyMsg.getMessage().getServices().stream()
                .map(Service::getName)
                .anyMatch(v -> v.equals(serviceName));
    }

    private ServiceReturn<CasualBuffer> toServiceReturn(CasualNWMessage<CasualServiceCallReplyMessage> v)
    {
        CasualServiceCallReplyMessage serviceReplyMessage = v.getMessage();
        return new ServiceReturn<>(serviceReplyMessage.getServiceBuffer(), (serviceReplyMessage.getError() == ErrorState.OK) ? ServiceReturnState.TPSUCCESS : ServiceReturnState.TPFAIL, serviceReplyMessage.getError(), serviceReplyMessage.getUserDefinedCode());
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
