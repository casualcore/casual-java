package se.laz.casual.network.grpc.outbound;

import com.google.protobuf.ByteString;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.grpc.MessageCreator;
import se.laz.casual.network.messages.CasualDomainConnectRequest;
import se.laz.casual.network.messages.CasualDomainDiscoveryRequest;
import se.laz.casual.network.messages.CasualPrepareRequest;
import se.laz.casual.network.messages.CasualRequest;
import se.laz.casual.network.messages.CasualServiceCallRequest;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareRequestMessage;

import java.util.List;
import java.util.stream.Collectors;

public final class RequestConverter
{
    private RequestConverter()
    {}

    public static <X extends CasualNetworkTransmittable>CasualRequest toCasualRequest(CasualNWMessage<X> message)
    {
        CasualRequest.Builder requestBuilder = CasualRequest.newBuilder()
                                                          .setCorrelationId(MessageCreator.toUUID4(message.getCorrelationId()))
                                                          .setMessageTypeValue(message.getType().getMessageId());
        X casualMessage = message.getMessage();
        if(casualMessage.getType() == CasualNWMessageType.DOMAIN_CONNECT_REQUEST)
        {
            return requestBuilder.setDomainConnect(convert((CasualDomainConnectRequestMessage)casualMessage)).build();
        }
        if(casualMessage.getType() == CasualNWMessageType.DOMAIN_DISCOVERY_REQUEST)
        {
            return requestBuilder.setDomainDiscovery(convert((CasualDomainDiscoveryRequestMessage)casualMessage)).build();
        }
        if(casualMessage.getType() == CasualNWMessageType.SERVICE_CALL_REQUEST)
        {
            return requestBuilder.setServiceCall(convert((CasualServiceCallRequestMessage)casualMessage)).build();
        }
        if(casualMessage.getType() == CasualNWMessageType.PREPARE_REQUEST)
        {
            return requestBuilder.setPrepare(convert((CasualTransactionResourcePrepareRequestMessage)casualMessage)).build();
        }
        throw new RuntimeException("bite me");
    }

    private static CasualDomainConnectRequest convert(CasualDomainConnectRequestMessage message)
    {
        return  CasualDomainConnectRequest.newBuilder()
                                          .setDomainId(MessageCreator.toUUID4(message.getDomainId()))
                                          .setExecution(MessageCreator.toUUID4(message.getExecution()))
                                          .setDomainName(message.getDomainName())
                                          .addAllProtocolVersion(message.getProtocols())
                                          .build();
    }

    private static CasualDomainDiscoveryRequest convert(CasualDomainDiscoveryRequestMessage message)
    {
        CasualDomainDiscoveryRequest.Builder builder = CasualDomainDiscoveryRequest.newBuilder()
                                                                                   .setDomainName(message.getDomainName())
                                                                                   .setDomainId(MessageCreator.toUUID4(message.getDomainId()))
                                                                                   .setExecution(MessageCreator.toUUID4(message.getExecution()));
        if(!message.getQueueNames().isEmpty())
        {
            builder.addAllQueueNames(message.getQueueNames());
        }
        if(!message.getServiceNames().isEmpty())
        {
            builder.addAllServiceNames(message.getServiceNames());
        }
        return builder.build();
    }

    private static CasualServiceCallRequest convert(CasualServiceCallRequestMessage message)
    {
        CasualServiceCallRequest.Builder builder = CasualServiceCallRequest.newBuilder()
                                                                           .setExecution(MessageCreator.toUUID4(message.getExecution()))
                                                                           .setServiceName(message.getServiceName())
                                                                           .setTimeout(message.getTimeout())
                                                                           .setXid(MessageCreator.toXID(message.getXid()))
                                                                           .setFlags(message.getXatmiFlags().getFlagValue())
                                                                           .setBufferTypeName(message.getServiceBuffer().getType())
                                                                           .setPayload(ByteString.copyFrom(toByteStringList(message.getServiceBuffer().getPayload())));
        if(!message.getParentName().isEmpty())
        {
            builder.setParentServiceName(message.getParentName());
        }
        return builder.build();
    }

    private static CasualPrepareRequest convert(CasualTransactionResourcePrepareRequestMessage message)
    {
        return CasualPrepareRequest.newBuilder()
                                   .setExecution(MessageCreator.toUUID4(message.getExecution()))
                                   .setXid(MessageCreator.toXID(message.getXid()))
                                   .setResourceManagerId(message.getResourceId())
                                   .setXaFlags(message.getFlags().getFlagValue())
                                   .build();
    }

    private static List<ByteString> toByteStringList(List<byte[]> payload)
    {
        return payload.stream()
                      .map(bytes -> ByteString.copyFrom(bytes))
                      .collect(Collectors.toList());
    }
}
