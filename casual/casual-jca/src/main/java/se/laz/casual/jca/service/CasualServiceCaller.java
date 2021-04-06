/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.service;

import com.google.protobuf.ByteString;
import se.laz.casual.api.CasualServiceApi;
import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.ServiceReturnState;
import se.laz.casual.jca.CasualManagedConnection;
import se.laz.casual.network.connection.CasualConnectionException;
import se.laz.casual.network.grpc.MessageCreator;
import se.laz.casual.network.messages.CasualDomainDiscoveryRequest;
import se.laz.casual.network.messages.CasualReply;
import se.laz.casual.network.messages.CasualRequest;
import se.laz.casual.network.messages.CasualServiceCallReply;
import se.laz.casual.network.messages.CasualServiceCallRequest;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        CompletableFuture<CasualReply> ff = makeServiceCall( UUID.randomUUID(), serviceName, data, flags);
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
        try
        {
            return serviceExists(UUID.randomUUID(), serviceName);
        }
        catch(Exception e)
        {
            throw new CasualConnectionException(e);
        }
    }

    private CompletableFuture<CasualReply> makeServiceCall(UUID corrid, String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        Duration timeout = Duration.of(connection.getTransactionTimeout(), ChronoUnit.SECONDS);
        CasualServiceCallRequest serviceRequestMessage = CasualServiceCallRequest.newBuilder()
                                                                                 .setExecution(MessageCreator.toUUID4(UUID.randomUUID()))
                                                                                 .setServiceName(serviceName)
                                                                                 .setXid(MessageCreator.toXID(connection.getCurrentXid()))
                                                                                 .setTimeout(timeout.toNanos())
                                                                                 .setFlags(flags.getFlagValue())
                                                                                 .setBufferTypeName(data.getType())
                                                                                 .setPayload(ByteString.copyFrom(data.getBytes().get(0)))
                                                                                 .build();
        CasualRequest requestEnvelope = CasualRequest.newBuilder()
                                                     .setMessageType(CasualRequest.MessageType.SERVICE_CALL_REQUEST)
                                                     .setCorrelationId(MessageCreator.toUUID4(corrid))
                                                     .setServiceCall(serviceRequestMessage)
                                                     .build();

        return connection.getNetworkConnection().request(requestEnvelope);
    }

    private boolean serviceExists( UUID corrid, String serviceName)
    {
        CasualDomainDiscoveryRequest requestMsg = CasualDomainDiscoveryRequest.newBuilder()
                                                                              .setExecution(MessageCreator.toUUID4(UUID.randomUUID()))
                                                                              .setDomainId(MessageCreator.toUUID4(UUID.randomUUID()))
                                                                              .setDomainName(connection.getDomainName())
                                                                              .addAllServiceNames(Arrays.asList(serviceName))
                                                                              .build();

        CasualRequest requestEnvelope = CasualRequest.newBuilder()
                                                     .setMessageType(CasualRequest.MessageType.DOMAIN_DISCOVERY_REQUEST)
                                                     .setCorrelationId(MessageCreator.toUUID4(corrid))
                                                     .setDomainDiscovery(requestMsg)
                                                     .build();
        CompletableFuture<CasualReply> replyMsgFuture = connection.getNetworkConnection().request(requestEnvelope);

        CasualReply replyMsg = replyMsgFuture.join();
        return replyMsg.getDomainDiscovery().getServicesList().stream()
                       .map(s ->  s.getName())
                       .anyMatch(v -> v.equals(serviceName));
    }

    private ServiceReturn<CasualBuffer> toServiceReturn(CasualReply v)
    {
        CasualServiceCallReply serviceReplyMessage = v.getServiceCall();
        byte[] payload = serviceReplyMessage.getPayload().toByteArray();
        List<byte[]> payloadData = new ArrayList<>();
        payloadData.add(payload);
        ServiceBuffer serviceBuffer = ServiceBuffer.of(serviceReplyMessage.getBufferTypeName(), payloadData);
        ErrorState errorState = ErrorState.unmarshal(serviceReplyMessage.getResult());
        return new ServiceReturn<>(serviceBuffer, (errorState == ErrorState.OK) ? ServiceReturnState.TPSUCCESS : ServiceReturnState.TPFAIL, errorState, serviceReplyMessage.getUser());
    }

    @Override
    public String toString()
    {
        return "CasualServiceCaller{" +
                "connection=" + connection +
                '}';
    }
}
