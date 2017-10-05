package se.kodarkatten.casual.jca.service;

import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.buffer.ServiceReturn;
import se.kodarkatten.casual.api.flags.AtmiFlags;
import se.kodarkatten.casual.api.flags.ErrorState;
import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.flags.ServiceReturnState;
import se.kodarkatten.casual.internal.buffer.CasualBufferBase;
import se.kodarkatten.casual.jca.CasualManagedConnection;
import se.kodarkatten.casual.network.connection.CasualConnectionException;
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

public class CasualServiceCaller
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

    public <X extends CasualBuffer> ServiceReturn<X> tpcall( String serviceName, X data, Flag<AtmiFlags> flags, Class<X> bufferClass )
    {
        final UUID corrid = UUID.randomUUID();
        if(serviceExists( corrid, serviceName))
        {
            return makeServiceCall( corrid, serviceName, data, flags, bufferClass);
        }
        throw new CasualConnectionException("service " + serviceName + " does not exist");
    }

    public <X extends CasualBuffer> CompletableFuture<ServiceReturn<X>> tpacall( String serviceName, X data, Flag<AtmiFlags> flags, Class<X> bufferClass )
    {
        throw new CasualConnectionException("not yet implemented");
    }

    private <X extends CasualBuffer> ServiceReturn<X> makeServiceCall( UUID corrid, String serviceName, X data, Flag<AtmiFlags> flags, Class<X> bufferClass)
    {
        CasualServiceCallRequestMessage serviceRequestMessage = CasualServiceCallRequestMessage.createBuilder()
                .setExecution(UUID.randomUUID())
                .setServiceBuffer(ServiceBuffer.of(data.getType(), data.getBytes()))
                .setServiceName(serviceName)
                .setXid( connection.getCurrentXid() )
                .setXatmiFlags(flags).build();

        CasualNWMessage<CasualServiceCallRequestMessage> serviceRequestNetworkMessage = CasualNWMessage.of(corrid, serviceRequestMessage);
        CasualNWMessage<CasualServiceCallReplyMessage> serviceReplyNetworkMessage = connection.getNetworkConnection().requestReply(serviceRequestNetworkMessage);
        CasualServiceCallReplyMessage serviceReplyMessage = serviceReplyNetworkMessage.getMessage();
        CasualBufferBase<X> buffer = CasualBufferBase.of(serviceReplyMessage.getServiceBuffer(), bufferClass);
        return new ServiceReturn<>(bufferClass.cast(buffer), (serviceReplyMessage.getError() == ErrorState.OK) ? ServiceReturnState.TPSUCCESS : ServiceReturnState.TPFAIL, serviceReplyMessage.getError());
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
