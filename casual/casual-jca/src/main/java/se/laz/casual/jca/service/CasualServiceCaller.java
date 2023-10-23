/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
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
import se.laz.casual.jca.CasualManagedConnection;
import se.laz.casual.network.connection.CasualConnectionException;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.laz.casual.network.protocol.messages.domain.Service;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;

import java.time.Duration;
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
    private CasualManagedConnection connection;

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
            validateTpCallFlags(serviceName, flags);
            return tpacall(serviceName, data, flags).join();
        }
        catch (Exception e)
        {
            throw new CasualConnectionException(e);
        }
    }

    @Override
    public CompletableFuture<ServiceReturn<CasualBuffer>> tpacall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        validateTpacallFlags(serviceName, flags);
        CompletableFuture<ServiceReturn<CasualBuffer>> f = new CompletableFuture<>();
        UUID corrId = UUID.randomUUID();
        boolean noReply = flags.isSet(AtmiFlags.TPNOREPLY);
        Optional<CompletableFuture<CasualNWMessage<CasualServiceCallReplyMessage>>> maybeServiceReturnValue = makeServiceCall(corrId, serviceName, data, flags, noReply);
        maybeServiceReturnValue.ifPresent(casualNWMessageCompletableFuture -> {
            casualNWMessageCompletableFuture.whenComplete((v, e) -> {
                if (null != e) {
                    LOG.finest(() -> "service call request failed for corrid: " + PrettyPrinter.casualStringify(corrId) + SERVICE_NAME_LITERAL + serviceName);
                    f.completeExceptionally(e);
                    return;
                }
                LOG.finest(() -> "service call request ok for corrid: " + PrettyPrinter.casualStringify(corrId) + SERVICE_NAME_LITERAL + serviceName);
                f.complete(toServiceReturn(v));
            });
        });
        if(noReply)
        {
            f.complete(ServiceReturn.NO_RETURN);
        }
        return f;
    }

    private void validateTpCallFlags(String serviceName, Flag<AtmiFlags> flags)
    {
        if(flags.isSet(AtmiFlags.TPNOREPLY))
        {
            throw new CasualProtocolException("tpCall to: + " + serviceName + " with TPNOREPLY - not allowed, should use tpacall");
        }
    }

    private void validateTpacallFlags(String serviceName, Flag<AtmiFlags> flags)
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

    private Optional<CompletableFuture<CasualNWMessage<CasualServiceCallReplyMessage>>> makeServiceCall(UUID corrid, String serviceName, CasualBuffer data, Flag<AtmiFlags> flags, boolean noReply)
    {
        Duration timeout = Duration.of(connection.getTransactionTimeout(), ChronoUnit.SECONDS);
        CasualServiceCallRequestMessage serviceRequestMessage = CasualServiceCallRequestMessage.createBuilder()
                .setExecution(UUID.randomUUID())
                .setServiceBuffer(ServiceBuffer.of(data))
                .setServiceName(serviceName)
                .setXid(connection.getCurrentXid())
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

    @Override
    public String toString()
    {
        return "CasualServiceCaller{" +
                "connection=" + connection +
                '}';
    }
}
