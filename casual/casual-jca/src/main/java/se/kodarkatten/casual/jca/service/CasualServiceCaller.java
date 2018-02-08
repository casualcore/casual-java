package se.kodarkatten.casual.jca.service;

import se.kodarkatten.casual.api.CasualServiceApi;
import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.buffer.ServiceReturn;
import se.kodarkatten.casual.api.flags.AtmiFlags;
import se.kodarkatten.casual.api.flags.ErrorState;
import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.flags.ServiceReturnState;
import se.kodarkatten.casual.jca.CasualManagedConnection;
import se.kodarkatten.casual.jca.CasualResourceAdapterException;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.kodarkatten.casual.network.messages.domain.Service;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallReplyMessage;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage;
import se.kodarkatten.casual.network.messages.service.ServiceBuffer;

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
    public ServiceReturn<CasualBuffer> tpcall( String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        try
        {
            return tpacall(serviceName, data, flags).get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualResourceAdapterException(e);
        }
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

    private CompletableFuture<CasualNWMessage<CasualServiceCallReplyMessage>> makeServiceCall( UUID corrid, String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        CasualServiceCallRequestMessage serviceRequestMessage = CasualServiceCallRequestMessage.createBuilder()
                .setExecution(UUID.randomUUID())
                .setServiceBuffer(ServiceBuffer.of(data))
                .setServiceName(serviceName)
                .setXid( connection.getCurrentXid() )
                .setXatmiFlags(flags).build();
        CasualNWMessage<CasualServiceCallRequestMessage> serviceRequestNetworkMessage = CasualNWMessage.of(corrid, serviceRequestMessage);
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
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> msg = CasualNWMessage.of(corrid, requestMsg);
        CompletableFuture<CasualNWMessage<CasualDomainDiscoveryReplyMessage>> replyMsgFuture = connection.getNetworkConnection().request(msg);
        try
        {
            CasualNWMessage<CasualDomainDiscoveryReplyMessage> replyMsg = replyMsgFuture.get();
            return replyMsg.getMessage().getServices().stream()
                           .map(Service::getName)
                           .anyMatch(v -> v.equals(serviceName));
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualResourceAdapterException(e);
        }
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
