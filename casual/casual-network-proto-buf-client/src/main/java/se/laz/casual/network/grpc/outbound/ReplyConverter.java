package se.laz.casual.network.grpc.outbound;

import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.grpc.MessageCreator;
import se.laz.casual.network.messages.CasualDomainConnectReply;
import se.laz.casual.network.messages.CasualReply;
import se.laz.casual.network.messages.UUID4;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage;
import se.laz.casual.network.protocol.messages.queue.CasualEnqueueReplyMessage;

public final class ReplyConverter
{
    private ReplyConverter()
    {}

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
