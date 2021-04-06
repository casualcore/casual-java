package se.laz.casual.network.grpc.outbound;

import com.google.protobuf.ByteString;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.grpc.MessageCreator;
import se.laz.casual.network.messages.CasualDomainConnectReply;
import se.laz.casual.network.messages.CasualDomainDiscoveryReply;
import se.laz.casual.network.messages.CasualReply;
import se.laz.casual.network.messages.CasualServiceCallReply;
import se.laz.casual.network.messages.Queue;
import se.laz.casual.network.messages.Service;
import se.laz.casual.network.messages.UUID4;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.laz.casual.network.protocol.messages.queue.CasualEnqueueReplyMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ReplyConverter
{
    private ReplyConverter()
    {}

    public static <X extends CasualNetworkTransmittable> CasualReply fromCasualNWMessage(CasualNWMessage<X> message)
    {
        CasualReply.Builder requestBuilder = CasualReply.newBuilder()
                                                        .setCorrelationId(MessageCreator.toUUID4(message.getCorrelationId()))
                                                        .setMessageTypeValue(message.getType().getMessageId());
        X casualMessage = message.getMessage();
        switch(message.getType())
        {
            case DOMAIN_CONNECT_REPLY:
                return requestBuilder.setDomainConnect(convert((CasualDomainConnectReplyMessage)casualMessage)).build();
            case DOMAIN_DISCOVERY_REPLY:
                return requestBuilder.setDomainDiscovery(convert((CasualDomainDiscoveryReplyMessage)casualMessage)).build();
            case SERVICE_CALL_REPLY:
                return requestBuilder.setServiceCall(convert((CasualServiceCallReplyMessage)casualMessage)).build();
            case ENQUEUE_REPLY:
                break;
            case DEQUEUE_REPLY:
                break;
            case PREPARE_REQUEST_REPLY:
                break;
            case COMMIT_REQUEST_REPLY:
                break;
            case REQUEST_ROLLBACK_REPLY:
                break;
            default:
                throw new ReplyConverterException("uknown message type: " + message.getType());
        }
        return null;
    }

    private static CasualServiceCallReply convert(CasualServiceCallReplyMessage reply)
    {
        CasualServiceCallReply.Builder builder = CasualServiceCallReply.newBuilder()
                              .setExecution(MessageCreator.toUUID4(reply.getExecution()))
                              .setXid(MessageCreator.toXID(reply.getXid()))
                              .setBufferTypeName(reply.getServiceBuffer().getType())
                              .setTransactionState(MessageCreator.toTransactionState(reply.getTransactionState()))
                              .setUser(reply.getUserDefinedCode())
                              .setResult(reply.getError().getValue());
        if(reply.getServiceBuffer().getPayload().size() == 1)
        {
            byte[] payload = reply.getServiceBuffer().getPayload().get(0);
            builder.setPayload(ByteString.copyFrom(payload));
        }
        return builder.build();
    }

    private static CasualDomainConnectReply convert(CasualDomainConnectReplyMessage reply)
    {
        return  CasualDomainConnectReply.newBuilder()
                                        .setExecution(MessageCreator.toUUID4(reply.getExecution()))
                                        .setDomainId(MessageCreator.toUUID4(reply.getDomainId()))
                                        .setDomainName(reply.getDomainName())
                                        .setProtocolVersion(reply.getProtocolVersion())
                                        .build();
    }

    private static CasualDomainDiscoveryReply convert(CasualDomainDiscoveryReplyMessage reply)
    {
        CasualDomainDiscoveryReply.Builder builder = CasualDomainDiscoveryReply.newBuilder()
                                                                               .setExecution(MessageCreator.toUUID4(reply.getExecution()))
                                                                               .setDomainId(MessageCreator.toUUID4(reply.getDomainId()))
                                                                               .setDomainName(reply.getDomainName());
        if(!reply.getQueues().isEmpty())
        {
            builder.addAllQueues(reply.getQueues().stream()
                                      .map(q -> Queue.newBuilder().setName(q.getName()).setRetries(q.getRetries()).build())
                                      .collect(Collectors.toList()));
        }
        if(!reply.getServices().isEmpty())
        {
            builder.addAllServices(reply.getServices().stream()
                                        .map(s -> Service.newBuilder()
                                                         .setCategory(s.getCategory())
                                                         .setHops(s.getHops())
                                                         .setName(s.getName())
                                                         .setTimeout(s.getTimeout())
                                                         .setTransactionType(MessageCreator.toTransactionType(s.getTransactionType()))
                                                         .build())
                                        .collect(Collectors.toList()));
        }
        return builder.build();
    }

    public static CasualNWMessage<?> toCasualNWMessage(CasualReply reply)
    {
        switch(reply.getMessageType())
        {
            case DOMAIN_CONNECT_REPLY:
                return domainConnect(reply);
            case DOMAIN_DISCOVERY_REPLY:
                return domainDiscovery(reply);
            case SERVICE_CALL_REPLY:
                return serviceCall(reply);
            case ENQUEUE_REPLY:
                return enqueue(reply);
            case DEQUEUE_REPLY:
                return dequeue(reply);
            case PREPARE_REPLY:
                return prepare(reply);
            case COMMIT_REPLY:
                return commit(reply);
            case ROLLBACK_REPLY:
                return rollback(reply);
            default:
                throw new ReplyConverterException("uknown message type: " + reply.getMessageType());
        }
    }

    private static CasualNWMessage<?> domainConnect(CasualReply reply)
    {
        CasualDomainConnectReply r = reply.getDomainConnect();
        CasualDomainConnectReplyMessage msg = CasualDomainConnectReplyMessage.createBuilder()
                                                                             .withExecution(MessageCreator.toUUID(r.getExecution()))
                                                                             .withDomainId(MessageCreator.toUUID(r.getDomainId()))
                                                                             .withDomainName(r.getDomainName())
                                                                             .withProtocolVersion(r.getProtocolVersion())
                                                                             .build();
        return create(reply.getCorrelationId(), msg);
    }

    private static CasualNWMessage<?> enqueue(CasualReply reply)
    {
        CasualEnqueueReplyMessage msg =  CasualEnqueueReplyMessage.createBuilder()
                                                                  .withExecution(MessageCreator.toUUID(reply.getEnqueue().getExecution()))
                                                                  .withId(MessageCreator.toUUID(reply.getEnqueue().getMessageId()))
                                                                  .build();
        return create(reply.getCorrelationId(), msg);
    }

    private static CasualNWMessage<?> dequeue(CasualReply reply)
    {
        return null;
    }

    private static CasualNWMessage<?> prepare(CasualReply reply)
    {
        return null;
    }

    private static CasualNWMessage<?> commit(CasualReply reply)
    {
        return null;
    }

    private static CasualNWMessage<?> rollback(CasualReply reply)
    {
        return null;
    }

    private static CasualNWMessage<?> serviceCall(CasualReply reply)
    {
        return null;
    }

    private static CasualNWMessage<?> domainDiscovery(CasualReply reply)
    {
        return null;
    }

    private static <T extends CasualNetworkTransmittable> CasualNWMessage<T> create(UUID4 correlationId, T msg)
    {
        return CasualNWMessageImpl.of(MessageCreator.toUUID(correlationId), msg);
    }
}
