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
import se.kodarkatten.casual.jca.service.work.FutureServiceReturnWork;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.kodarkatten.casual.network.messages.domain.Service;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallReplyMessage;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage;
import se.kodarkatten.casual.network.messages.service.ServiceBuffer;

import javax.resource.spi.work.WorkException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
        return makeServiceCall( UUID.randomUUID(), serviceName, data, flags);
    }

    @Override
    public CompletableFuture<ServiceReturn<CasualBuffer>> tpacall( String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        CompletableFuture<ServiceReturn<CasualBuffer>> f = new CompletableFuture<>();
        try
        {
            connection.getWorkManager().scheduleWork(FutureServiceReturnWork.of(f, () -> tpcall(serviceName, data, flags)));
        }
        catch (WorkException e)
        {
            f.completeExceptionally(new CasualResourceAdapterException("tpacall, failed dispatching work calling service: " + serviceName, e));
        }
        return f;
    }

    @Override
    public boolean serviceExists(String serviceName)
    {
        return serviceExists(UUID.randomUUID(), serviceName);
    }

    private ServiceReturn<CasualBuffer> makeServiceCall( UUID corrid, String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        CasualServiceCallRequestMessage serviceRequestMessage = CasualServiceCallRequestMessage.createBuilder()
                .setExecution(UUID.randomUUID())
                .setServiceBuffer(ServiceBuffer.of(data))
                .setServiceName(serviceName)
                .setXid( connection.getCurrentXid() )
                .setXatmiFlags(flags).build();
        CasualNWMessage<CasualServiceCallRequestMessage> serviceRequestNetworkMessage = CasualNWMessage.of(corrid, serviceRequestMessage);
        CasualNWMessage<CasualServiceCallReplyMessage> serviceReplyNetworkMessage = connection.getNetworkConnection().requestReply(serviceRequestNetworkMessage);
        CasualServiceCallReplyMessage serviceReplyMessage = serviceReplyNetworkMessage.getMessage();
        return new ServiceReturn<>(serviceReplyMessage.getServiceBuffer(), (serviceReplyMessage.getError() == ErrorState.OK) ? ServiceReturnState.TPSUCCESS : ServiceReturnState.TPFAIL, serviceReplyMessage.getError(), serviceReplyMessage.getUserDefinedCode());
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
        CasualNWMessage<CasualDomainDiscoveryReplyMessage> replyMsg = connection.getNetworkConnection().requestReply(msg);
        return replyMsg.getMessage().getServices().stream()
                .map( Service::getName )
                .anyMatch(v-> v.equals( serviceName ) );
    }

    @Override
    public String toString()
    {
        return "CasualServiceCaller{" +
                "connection=" + connection +
                '}';
    }
}
