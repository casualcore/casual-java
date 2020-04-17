package se.laz.casual.network.grpc;

import com.google.protobuf.ByteString;
import se.laz.casual.api.util.time.InstantUtil;
import se.laz.casual.api.xa.XID;
import se.laz.casual.network.messages.CasualCommitReply;
import se.laz.casual.network.messages.CasualCommitRequest;
import se.laz.casual.network.messages.CasualDequeueReply;
import se.laz.casual.network.messages.CasualDequeueRequest;
import se.laz.casual.network.messages.CasualDomainConnectReply;
import se.laz.casual.network.messages.CasualDomainConnectRequest;
import se.laz.casual.network.messages.CasualDomainDiscoveryReply;
import se.laz.casual.network.messages.CasualDomainDiscoveryRequest;
import se.laz.casual.network.messages.CasualEnqueueReply;
import se.laz.casual.network.messages.CasualEnqueueRequest;
import se.laz.casual.network.messages.CasualPrepareReply;
import se.laz.casual.network.messages.CasualPrepareRequest;
import se.laz.casual.network.messages.CasualReply;
import se.laz.casual.network.messages.CasualRequest;
import se.laz.casual.network.messages.CasualRollbackReply;
import se.laz.casual.network.messages.CasualRollbackRequest;
import se.laz.casual.network.messages.CasualServiceCallReply;
import se.laz.casual.network.messages.CasualServiceCallRequest;
import se.laz.casual.network.messages.DequeueMessage;
import se.laz.casual.network.messages.Queue;
import se.laz.casual.network.messages.QueueMessage;
import se.laz.casual.network.messages.Selector;
import se.laz.casual.network.messages.Service;
import se.laz.casual.network.messages.State;
import se.laz.casual.network.messages.TransactionState;
import se.laz.casual.network.messages.UUID4;
import se.laz.casual.network.messages.XIDGRPC;

import javax.transaction.xa.Xid;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class MessageCreator
{
    private MessageCreator()
    {}

    public static CasualDomainConnectRequest createCasualDomainConnectRequest(UUID4 execution, UUID4 domainId, String domainName, long protocolVersion)
    {
        return CasualDomainConnectRequest.newBuilder()
                                         .setExecution(execution)
                                         .setDomainId(domainId)
                                         .setDomainName(domainName)
                                         .setProtocolVersion(protocolVersion)
                                         .build();
    }

    public static CasualDomainConnectReply createCasualDomainConnectReply(UUID4 execution, UUID4 domainId, String domainName, long protocolVersion)
    {
        return CasualDomainConnectReply.newBuilder()
                                       .setExecution(execution)
                                       .setDomainId(domainId)
                                       .setDomainName(domainName)
                                       .setProtocolVersion(protocolVersion)
                                       .build();
    }


    public static CasualDomainDiscoveryRequest createCasualDomainDiscoveryRequest(UUID4 execution, UUID4 domainId, String domainName, Optional<List<String>> serviceNames, Optional<List<String>> queueNames)
    {
        CasualDomainDiscoveryRequest.Builder builder =  CasualDomainDiscoveryRequest.newBuilder()
                                                                                    .setExecution(execution)
                                                                                    .setDomainId(domainId)
                                                                                    .setDomainName(domainName);
        serviceNames.ifPresent(s -> builder.addAllServiceNames(s));
        queueNames.ifPresent(q -> builder.addAllQueueNames(q));
        return builder.build();
    }

    public static CasualDomainDiscoveryReply createCasualDomainDiscoveryReply(UUID4 execution, UUID4 domainId, String domainName, Optional<List<Service>> services, Optional<List<Queue>> queues)
    {
        CasualDomainDiscoveryReply.Builder builder =  CasualDomainDiscoveryReply.newBuilder()
                                                                                    .setExecution(execution)
                                                                                    .setDomainId(domainId)
                                                                                    .setDomainName(domainName);
        services.ifPresent(s -> builder.addAllServices(s));
        queues.ifPresent(q -> builder.addAllQueues(q));
        return builder.build();
    }

    public static CasualServiceCallRequest createCasualServiceCallRequest(UUID4 execution, String serviceName, long timeout, Optional<String> parentServiceName, XIDGRPC xid, long flags, String bufferTypeName, byte[] payload)
    {
        CasualServiceCallRequest.Builder builder =  CasualServiceCallRequest.newBuilder()
                                                                            .setExecution(execution)
                                                                            .setServiceName(serviceName)
                                                                            .setTimeout(timeout)
                                                                            .setXid(xid)
                                                                            .setFlags(flags)
                                                                            .setBufferTypeName(bufferTypeName)
                                                                            .setPayload(ByteString.copyFrom(payload));
        parentServiceName.ifPresent(name -> builder.setParentServiceName(name));
        return builder.build();
    }

    public static CasualServiceCallReply createCasualServiceCallReply(UUID4 execution, int result, long user, XIDGRPC xid, TransactionState transactionState, String bufferTypeName, byte[] payload)
    {
        return CasualServiceCallReply.newBuilder()
                                     .setExecution(execution)
                                     .setResult(result)
                                     .setUser(user)
                                     .setXid(xid)
                                     .setTransactionState(transactionState)
                                     .setBufferTypeName(bufferTypeName)
                                     .setPayload(ByteString.copyFrom(payload))
                                     .build();
    }

    public static CasualPrepareRequest createCasualPrepareRequest(UUID4 execution, XIDGRPC xid, int resourceManagerId, long xaFlags)
    {
        return CasualPrepareRequest.newBuilder()
                                   .setExecution(execution)
                                   .setXid(xid)
                                   .setResourceManagerId(resourceManagerId)
                                   .setXaFlags(xaFlags)
                                   .build();
    }

    public static CasualPrepareReply createCasualPrepareReply(UUID4 execution, XIDGRPC xid, int resourceManagerId, State state)
    {
        return CasualPrepareReply.newBuilder()
                                 .setExecution(execution)
                                 .setXid(xid)
                                 .setResourceManagerId(resourceManagerId)
                                 .setState(state)
                                 .build();
    }

    public static CasualCommitRequest createCasualCommitRequest(UUID4 execution, XIDGRPC xid, int resourceManagerId, long xaFlags)
    {
        return CasualCommitRequest.newBuilder()
                                  .setExecution(execution)
                                  .setXid(xid)
                                  .setResourceManagerId(resourceManagerId)
                                  .setXaFlags(xaFlags)
                                  .build();
    }

    public static CasualCommitReply createCasualCommitReply(UUID4 execution, XIDGRPC xid, int resourceManagerId, State state)
    {
        return CasualCommitReply.newBuilder()
                                .setExecution(execution)
                                .setXid(xid)
                                .setResourceManagerId(resourceManagerId)
                                .setState(state)
                                .build();
    }

    public static CasualRollbackRequest createCasualRollbackRequest(UUID4 execution, XIDGRPC xid, int resourceManagerId, long xaFlags)
    {
        return CasualRollbackRequest.newBuilder()
                                    .setExecution(execution)
                                    .setXid(xid)
                                    .setResourceManagerId(resourceManagerId)
                                    .setXaFlags(xaFlags)
                                    .build();
    }

    public static CasualRollbackReply createCasualRollbackReply(UUID4 execution, XIDGRPC xid, int resourceManagerId, State state)
    {
        return CasualRollbackReply.newBuilder()
                                  .setExecution(execution)
                                  .setXid(xid)
                                  .setResourceManagerId(resourceManagerId)
                                  .setState(state)
                                  .build();
    }

    public static CasualEnqueueRequest createCasualEnqueueRequest(UUID4 execution, String queueName, XIDGRPC xid, QueueMessage message)
    {
        return CasualEnqueueRequest.newBuilder()
                                   .setExecution(execution)
                                   .setQueueName(queueName)
                                   .setXid(xid)
                                   .setMessage(message)
                                   .build();
    }

    public static CasualEnqueueReply createCasualEnqueueReply(UUID4 execution, UUID4 msgId)
    {
        return CasualEnqueueReply.newBuilder()
                                 .setExecution(execution)
                                 .setMessageId(msgId)
                                 .build();
    }

    public static CasualDequeueRequest createCasualDequeueRequest(UUID4 execution, String queueName, XIDGRPC xid, Selector selector, boolean block)
    {
        return CasualDequeueRequest.newBuilder()
                                   .setExecution(execution)
                                   .setQueueName(queueName)
                                   .setXid(xid)
                                   .setSelector(selector)
                                   .setBlock(block)
                                   .build();
    }

    public static CasualDequeueReply CasualDequeueReply(UUID4 execution, List<DequeueMessage> messages)
    {
        return CasualDequeueReply.newBuilder()
                                 .setExecution(execution)
                                 .addAllMessage(messages)
                                 .build();
    }

    // Helpers
    public static UUID4 toUUID4(UUID id)
    {
        return UUID4.newBuilder()
                    .setMostSignificantBits(id.getMostSignificantBits())
                    .setLeastSignificantBits(id.getLeastSignificantBits())
                    .build();
    }

    public static UUID toUUID(UUID4 id)
    {
        return new UUID(id.getMostSignificantBits(), id.getLeastSignificantBits());
    }

    public static XIDGRPC toXIDGRPC(Xid in)
    {
        XID xid;
        if(in instanceof XID)
        {
            xid = (XID)in;
        }
        else
        {
            throw new MessageCreatorException("Xid not an implementation of XID, bailing");
        }
        return XIDGRPC.newBuilder()
                      .setXidGtridLength(xid.getGtridLength())
                      .setXidBqualLength(xid.getBqualLength())
                      .setXidFormat(xid.getFormatId())
                      .setXidData(ByteString.copyFrom(xid.getData()))
                      .build();
    }

    public static Xid toXID(XIDGRPC xid)
    {
        return XID.of(Math.toIntExact(xid.getXidGtridLength()), Math.toIntExact(xid.getXidBqualLength()), xid.getXidData().toByteArray(), xid.getXidFormat());
    }

    public static QueueMessage createQueueMessage(UUID id, Optional<String> properties, Optional<String> replyQueue, Instant availableSince, String type, byte[] payload)
    {
        QueueMessage.Builder builder =  QueueMessage.newBuilder()
                                                    .setId(toUUID4(id))
                                                    .setAvailableSince(InstantUtil.toNanos(availableSince))
                                                    .setType(type)
                                                    .setPayload(ByteString.copyFrom(payload));
        properties.ifPresent(p -> builder.setProperties(p));
        replyQueue.ifPresent(q -> builder.setReplyQueue(q));
        return builder.build();
    }

    public static DequeueMessage createDequeueMessage(UUID id, Optional<String> properties, Optional<String> replyQueue, Instant availableSince, String type, byte[] payload, long redeliveredCount, Instant timestamp)
    {
        DequeueMessage.Builder builder =  DequeueMessage.newBuilder()
                                                        .setId(toUUID4(id))
                                                        .setAvailableSince(InstantUtil.toNanos(availableSince))
                                                        .setType(type)
                                                        .setPayload(ByteString.copyFrom(payload))
                                                        .setRedeliveredCount(redeliveredCount)
                                                        .setTimestamp(InstantUtil.toNanos(timestamp));
        properties.ifPresent(p -> builder.setProperties(p));
        replyQueue.ifPresent(q -> builder.setReplyQueue(q));
        return builder.build();
    }

    public static Selector createSelector(UUID id, Optional<String> properties)
    {
        Selector.Builder builder =  Selector.newBuilder()
                                            .setId(toUUID4(id));
        properties.ifPresent(p -> builder.setProperties(p));
        return builder.build();
    }

    public static CasualRequest.Builder createRequestBuilder(CasualRequest.MessageType messageType, UUID4 correlationId)
    {
        return CasualRequest.newBuilder()
                            .setMessageType(messageType)
                            .setCorrelationId(correlationId);
    }

    public static CasualReply.Builder createReplyBuilder(CasualReply.MessageType messageType, UUID4 correlationId)
    {
        return CasualReply.newBuilder()
                          .setMessageType(messageType)
                          .setCorrelationId(correlationId);
    }

}
