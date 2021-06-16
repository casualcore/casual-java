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
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class CasualServiceCaller implements CasualServiceApi
{
    private final static Logger LOG = Logger.getLogger(CasualServiceCaller.class.getName());
    private CasualManagedConnection connection;

    private CasualServiceCaller( CasualManagedConnection connection )
    {
        this.connection = connection;
    }

    public static CasualServiceCaller of( CasualManagedConnection connection )
    {
        return new CasualServiceCaller( connection );
    }

    @Override
    public ServiceReturn<CasualBuffer> tpcall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        try
        {
            return tpacall(serviceName, data, flags).join();
        }
        catch(Exception e)
        {
            throw new CasualConnectionException(e);
        }
    }

    @Override
    public CompletableFuture<ServiceReturn<CasualBuffer>> tpacall( String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        CompletableFuture<ServiceReturn<CasualBuffer>> f = new CompletableFuture<>();
        UUID corrId = UUID.randomUUID();
        CompletableFuture<CasualNWMessage<CasualServiceCallReplyMessage>> ff = makeServiceCall( corrId, serviceName, data, flags);
        ff.whenComplete((v, e) ->{
            if(null != e)
            {
                LOG.finest(()->"service call request failed for corrid: " + corrId + " serviceName: " + serviceName);
                f.completeExceptionally(e);
                return;
            }
            LOG.finest(()->"service call request ok for corrid: " + corrId + " serviceName: " + serviceName);
            f.complete(toServiceReturn(v));
        });
        return f;
    }

    @Override
    public boolean serviceExists(String serviceName)
    {
        try
        {
            return serviceExists(UUID.randomUUID(), serviceName);
        }
        catch(Exception e)
        {
            throw new CasualConnectionException(e);
        }
    }

    private CompletableFuture<CasualNWMessage<CasualServiceCallReplyMessage>> makeServiceCall(UUID corrid, String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        Duration timeout = Duration.of(connection.getTransactionTimeout(), ChronoUnit.SECONDS);
        CasualServiceCallRequestMessage serviceRequestMessage = CasualServiceCallRequestMessage.createBuilder()
                .setExecution(UUID.randomUUID())
                .setServiceBuffer(ServiceBuffer.of(data))
                .setServiceName(serviceName)
                .setXid( connection.getCurrentXid() )
                .setTimeout(timeout.toNanos())
                .setXatmiFlags(flags).build();
        CasualNWMessage<CasualServiceCallRequestMessage> serviceRequestNetworkMessage = CasualNWMessageImpl.of(corrid, serviceRequestMessage);
        LOG.finest(()->"issuing service call reequest, corrid: " + corrid + " serviceName: " + serviceName);
        return connection.getNetworkConnection().request(serviceRequestNetworkMessage);
    }

    private boolean serviceExists( UUID corrid, String serviceName)
    {
        LOG.finest(()->"issuing domain discovery, corrid: " + corrid + " serviceName: " + serviceName);
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
        LOG.finest(()->"domain discovery ok for corrid: " + corrid + " serviceName: " + serviceName);
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
