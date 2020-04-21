package se.laz.casual.network.grpc.outbound;

import com.google.protobuf.ByteString;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.api.util.time.InstantUtil;
import se.laz.casual.network.grpc.MessageCreator;
import se.laz.casual.network.messages.CasualCommitRequest;
import se.laz.casual.network.messages.CasualDequeueRequest;
import se.laz.casual.network.messages.CasualDomainConnectRequest;
import se.laz.casual.network.messages.CasualDomainDiscoveryRequest;
import se.laz.casual.network.messages.CasualEnqueueRequest;
import se.laz.casual.network.messages.CasualPrepareRequest;
import se.laz.casual.network.messages.CasualRequest;
import se.laz.casual.network.messages.CasualRollbackRequest;
import se.laz.casual.network.messages.CasualServiceCallRequest;
import se.laz.casual.network.messages.QueueMessage;
import se.laz.casual.network.messages.Selector;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.laz.casual.network.protocol.messages.queue.CasualDequeueRequestMessage;
import se.laz.casual.network.protocol.messages.queue.CasualEnqueueRequestMessage;
import se.laz.casual.network.protocol.messages.queue.EnqueueMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackRequestMessage;

import java.util.List;
import java.util.UUID;
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
        if(casualMessage.getType() == CasualNWMessageType.COMMIT_REQUEST)
        {
            return requestBuilder.setCommit(convert((CasualTransactionResourceCommitRequestMessage)casualMessage)).build();
        }
        if(casualMessage.getType() == CasualNWMessageType.REQUEST_ROLLBACK)
        {
            return requestBuilder.setRollback(convert((CasualTransactionResourceRollbackRequestMessage)casualMessage)).build();
        }
        if(casualMessage.getType() == CasualNWMessageType.ENQUEUE_REQUEST)
        {
            return requestBuilder.setEnqueue(convert((CasualEnqueueRequestMessage)casualMessage)).build();
        }
        if(casualMessage.getType() == CasualNWMessageType.DEQUEUE_REQUEST)
        {
            return requestBuilder.setDequeue(convert((CasualDequeueRequestMessage)casualMessage)).build();
        }
        throw new RequestConverterException("unknown request type:" + casualMessage.getType());
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

    private static CasualCommitRequest convert(CasualTransactionResourceCommitRequestMessage message)
    {
        return CasualCommitRequest.newBuilder()
                                  .setExecution(MessageCreator.toUUID4(message.getExecution()))
                                  .setXid(MessageCreator.toXID(message.getXid()))
                                  .setResourceManagerId(message.getResourceId())
                                  .setXaFlags(message.getFlags().getFlagValue())
                                  .build();
    }

    private static CasualRollbackRequest convert(CasualTransactionResourceRollbackRequestMessage message)
    {
        return CasualRollbackRequest.newBuilder()
                                    .setExecution(MessageCreator.toUUID4(message.getExecution()))
                                    .setXid(MessageCreator.toXID(message.getXid()))
                                    .setResourceManagerId(message.getResourceId())
                                    .setXaFlags(message.getFlags().getFlagValue())
                                    .build();
    }

    private static CasualEnqueueRequest convert(CasualEnqueueRequestMessage message)
    {
        return CasualEnqueueRequest.newBuilder()
                                   .setExecution(MessageCreator.toUUID4(message.getExecution()))
                                   .setQueueName(message.getQueueName())
                                   .setXid(MessageCreator.toXID(message.getXid()))
                                   .setMessage(toQueueMessage(message.getMessage()))
                                   .build();
    }

    private static CasualDequeueRequest convert(CasualDequeueRequestMessage message)
    {
        return CasualDequeueRequest.newBuilder()
                                   .setExecution(MessageCreator.toUUID4(message.getExecution()))
                                   .setQueueName(message.getQueueName())
                                   .setXid(MessageCreator.toXID(message.getXid()))
                                   .setSelector(toSelector(message.getSelectorProperties(), message.getSelectorUUID()))
                                   .setBlock(message.isBlock())
                                   .build();
    }

    private static Selector toSelector(String selectorProperties, UUID selectorUUID)
    {
        return Selector.newBuilder()
                       .setId(MessageCreator.toUUID4(selectorUUID))
                       .setProperties(selectorProperties)
                       .build();
    }

    private static QueueMessage toQueueMessage(EnqueueMessage message)
    {
        QueueMessage.Builder builder =  QueueMessage.newBuilder()
                                                    .setId(MessageCreator.toUUID4(message.getId()))
                                                    .setType(message.getPayload().getType())
                                                    .setPayload(ByteString.copyFrom(toByteStringList(message.getPayload().getPayload())))
                                                    .setAvailableSince(InstantUtil.toNanos(message.getAvailableSince()));
        if(!message.getCorrelationInformation().isEmpty())
        {
            builder.setProperties(message.getCorrelationInformation());
        }
        if(!message.getReplyQueue().isEmpty())
        {
            builder.setReplyQueue(message.getReplyQueue());
        }
        return builder.build();
    }

    private static List<ByteString> toByteStringList(List<byte[]> payload)
    {
        return payload.stream()
                      .map(bytes -> ByteString.copyFrom(bytes))
                      .collect(Collectors.toList());
    }
}
