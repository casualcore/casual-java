/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.service;

import se.laz.casual.api.CasualServiceApi;
import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.ServiceReturnState;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.jca.CasualManagedConnection;
import se.laz.casual.jca.CasualResourceAdapterException;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.laz.casual.network.protocol.messages.domain.Service;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;
import se.laz.casual.api.buffer.type.ServiceBuffer;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CasualServiceCaller implements CasualServiceApi
{
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
        return tpacall(serviceName, data, flags).join();
    }

    @Override
    public CompletableFuture<ServiceReturn<CasualBuffer>> tpacall( String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        CompletableFuture<ServiceReturn<CasualBuffer>> f = new CompletableFuture<>();
        CompletableFuture<CasualNWMessage<CasualServiceCallReplyMessage>> ff = makeServiceCall( UUID.randomUUID(), serviceName, data, flags);
        ff.whenComplete((v, e) ->{
            if(null != e)
            {
                f.completeExceptionally(e);
                return;
            }
            f.complete(toServiceReturn(v));
        });
        return f;
    }

    @Override
    public boolean serviceExists(String serviceName)
    {
        return serviceExists(UUID.randomUUID(), serviceName);
    }

    private CompletableFuture<CasualNWMessage<CasualServiceCallReplyMessage>> makeServiceCall(UUID corrid, String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        CasualServiceCallRequestMessage serviceRequestMessage = CasualServiceCallRequestMessage.createBuilder()
                .setExecution(UUID.randomUUID())
                .setServiceBuffer(ServiceBuffer.of(data))
                .setServiceName(serviceName)
                .setXid( connection.getCurrentXid() )
                .setXatmiFlags(flags).build();
        CasualNWMessage<CasualServiceCallRequestMessage> serviceRequestNetworkMessage = CasualNWMessageImpl.of(corrid, serviceRequestMessage);
        return connection.getNetworkConnection().request(serviceRequestNetworkMessage);
    }

    private boolean serviceExists( UUID corrid, String serviceName)
    {
        CasualDomainDiscoveryRequestMessage requestMsg = CasualDomainDiscoveryRequestMessage.createBuilder()
                                                                                            .setExecution(UUID.randomUUID())
                                                                                            .setDomainId(UUID.randomUUID())
                                                                                            .setDomainName( connection.getDomainName() )
                                                                                            .setServiceNames(Arrays.asList(serviceName))
                                                                                            .build();
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> msg = CasualNWMessageImpl.of(corrid, requestMsg);
        CompletableFuture<CasualNWMessage<CasualDomainDiscoveryReplyMessage>> replyMsgFuture = connection.getNetworkConnection().request(msg);

        CasualNWMessage<CasualDomainDiscoveryReplyMessage> replyMsg = replyMsgFuture.join();
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
