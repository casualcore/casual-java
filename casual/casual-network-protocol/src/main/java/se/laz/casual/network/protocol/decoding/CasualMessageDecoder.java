/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding;


import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.protocol.decoding.decoders.CasualNWMessageHeaderDecoder;
import se.laz.casual.network.protocol.decoding.decoders.MessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.conversation.ConnectReplyMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.conversation.ConnectRequestMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.conversation.DisconnectMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.conversation.RequestMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.domain.CasualDomainConnectReplyMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.domain.CasualDomainConnectRequestMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.domain.CasualDomainDiscoveryReplyMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.domain.CasualDomainDiscoveryRequestMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.domain.DomainDisconnectReplyMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.domain.DomainDisconnectRequestMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.queue.CasualDequeueReplyMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.queue.CasualDequeueRequestMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.queue.CasualEnqueueReplyMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.queue.CasualEnqueueRequestMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.service.CasualServiceCallReplyMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.service.CasualServiceCallRequestMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.transaction.CasualTransactionResourceCommitReplyMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.transaction.CasualTransactionResourceCommitRequestMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.transaction.CasualTransactionResourcePrepareReplyMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.transaction.CasualTransactionResourcePrepareRequestMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.transaction.CasualTransactionResourceRollbackReplyMessageDecoder;
import se.laz.casual.network.protocol.decoding.decoders.transaction.CasualTransactionResourceRollbackRequestMessageDecoder;
import se.laz.casual.network.protocol.messages.CasualNWMessageHeader;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;

public final class CasualMessageDecoder
{
    private static int maxSingleBufferByteSize = Integer.MAX_VALUE;
    private CasualMessageDecoder()
    {}

    /**
     * Number of maximum bytes before any chunk reading takes place
     * Defaults to Integer.MAX_VALUE
     * @return
     */
    public static int getMaxSingleBufferByteSize()
    {
        return maxSingleBufferByteSize;
    }

    public static CasualNWMessageHeader networkHeaderToCasualHeader(final byte[] message)
    {
        return CasualNWMessageHeaderDecoder.fromNetworkBytes(message);
    }

    public static <T extends CasualNetworkTransmittable> CasualNWMessage<T> read(final byte[] data, CasualNWMessageHeader header)
    {
        NetworkDecoder<T> networkReader = getDecoder( header );
        return readMessage( data, header, networkReader );
    }

    @SuppressWarnings({"unchecked", "squid:MethodCyclomaticComplexity"})
    static <T extends CasualNetworkTransmittable> NetworkDecoder<T> getDecoder(CasualNWMessageHeader header )
    {
        switch(header.getType())
        {
            case DOMAIN_DISCOVERY_REQUEST:
                return (NetworkDecoder<T>) CasualDomainDiscoveryRequestMessageDecoder.of();
            case DOMAIN_DISCOVERY_REPLY:
                return (NetworkDecoder<T>) CasualDomainDiscoveryReplyMessageDecoder.of();
            case DOMAIN_DISCONNECT_REQUEST:
                return (NetworkDecoder<T>) DomainDisconnectRequestMessageDecoder.of();
            case DOMAIN_DISCONNECT_REPLY:
                return (NetworkDecoder<T>) DomainDisconnectReplyMessageDecoder.of();
            case DOMAIN_CONNECT_REQUEST:
                return (NetworkDecoder<T>) CasualDomainConnectRequestMessageDecoder.of();
            case DOMAIN_CONNECT_REPLY:
                return (NetworkDecoder<T>) CasualDomainConnectReplyMessageDecoder.of();
            case SERVICE_CALL_REQUEST:
                // We may want to use some other size for chunking of service payload
                CasualServiceCallRequestMessageDecoder.setMaxPayloadSingleBufferByteSize(getMaxSingleBufferByteSize());
                return (NetworkDecoder<T>) CasualServiceCallRequestMessageDecoder.of();
            case SERVICE_CALL_REPLY:
                // We may want to use some other size for chunking of service payload
                CasualServiceCallReplyMessageDecoder.setMaxPayloadSingleBufferByteSize(getMaxSingleBufferByteSize());
                return (NetworkDecoder<T>) CasualServiceCallReplyMessageDecoder.of();
            case ENQUEUE_REQUEST:
                return (NetworkDecoder<T>) CasualEnqueueRequestMessageDecoder.of();
            case ENQUEUE_REPLY:
                return (NetworkDecoder<T>) CasualEnqueueReplyMessageDecoder.of();
            case DEQUEUE_REQUEST:
                return (NetworkDecoder<T>) CasualDequeueRequestMessageDecoder.of();
            case DEQUEUE_REPLY:
                return (NetworkDecoder<T>) CasualDequeueReplyMessageDecoder.of();
            case PREPARE_REQUEST:
                return (NetworkDecoder<T>) CasualTransactionResourcePrepareRequestMessageDecoder.of();
            case PREPARE_REQUEST_REPLY:
                return (NetworkDecoder<T>) CasualTransactionResourcePrepareReplyMessageDecoder.of();
            case COMMIT_REQUEST:
                return (NetworkDecoder<T>) CasualTransactionResourceCommitRequestMessageDecoder.of();
            case COMMIT_REQUEST_REPLY:
                return (NetworkDecoder<T>) CasualTransactionResourceCommitReplyMessageDecoder.of();
            case REQUEST_ROLLBACK:
                return (NetworkDecoder<T>) CasualTransactionResourceRollbackRequestMessageDecoder.of();
            case REQUEST_ROLLBACK_REPLY:
                return (NetworkDecoder<T>) CasualTransactionResourceRollbackReplyMessageDecoder.of();
            case CONVERSATION_CONNECT:
                return (NetworkDecoder<T>) ConnectRequestMessageDecoder.of();
            case CONVERSATION_CONNECT_REPLY:
                return (NetworkDecoder<T>) ConnectReplyMessageDecoder.of();
            case CONVERSATION_REQUEST:
                return (NetworkDecoder<T>) RequestMessageDecoder.of();
            case CONVERSATION_DISCONNECT:
                return (NetworkDecoder<T>) DisconnectMessageDecoder.of();
            default:
                throw new UnsupportedOperationException("Unknown messagetype: " + header.getType());
        }
    }

    private static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readMessage(final byte[] data, final CasualNWMessageHeader header, NetworkDecoder<T> nr )
    {
        final MessageDecoder<T> reader = MessageDecoder.of(nr, getMaxSingleBufferByteSize() );
        final T msg = reader.read(data);
        return CasualNWMessageImpl.of(header.getCorrelationId(), msg);
    }

}
