package se.kodarkatten.casual.network.io;


import se.kodarkatten.casual.network.io.readers.CasualNWMessageHeaderReader;
import se.kodarkatten.casual.network.io.readers.MessageReader;
import se.kodarkatten.casual.network.io.readers.domain.CasualDomainDiscoveryReplyMessageReader;
import se.kodarkatten.casual.network.io.readers.domain.CasualDomainDiscoveryRequestMessageReader;
import se.kodarkatten.casual.network.io.readers.service.CasualServiceCallReplyMessageReader;
import se.kodarkatten.casual.network.io.readers.service.CasualServiceCallRequestMessageReader;
import se.kodarkatten.casual.network.io.readers.transaction.CasualTransactionResourceCommitReplyMessageReader;
import se.kodarkatten.casual.network.io.readers.transaction.CasualTransactionResourceCommitRequestMessageReader;
import se.kodarkatten.casual.network.io.readers.transaction.CasualTransactionResourcePrepareReplyMessageReader;
import se.kodarkatten.casual.network.io.readers.transaction.CasualTransactionResourcePrepareRequestMessageReader;
import se.kodarkatten.casual.network.io.readers.transaction.CasualTransactionResourceRollbackReplyMessageReader;
import se.kodarkatten.casual.network.io.readers.transaction.CasualTransactionResourceRollbackRequestMessageReader;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.CasualNWMessageHeader;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.messages.parseinfo.MessageHeaderSizes;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallReplyMessage;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceCommitReplyMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceCommitRequestMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourcePrepareReplyMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourcePrepareRequestMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceRollbackReplyMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceRollbackRequestMessage;
import se.kodarkatten.casual.network.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class CasualNetworkReader
{
    private static int maxSingleBufferByteSize = Integer.MAX_VALUE;
    private CasualNetworkReader()
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

    /**
     * If not set, defaults to Integer.MAX_VALUE
     * Can be used in testing to force chunked reading
     * by for instance setting it to 1
     * @return
     */
    public static void setMaxSingleBufferByteSize(int maxSingleBufferByteSize)
    {
        CasualNetworkReader.maxSingleBufferByteSize = maxSingleBufferByteSize;
    }

    /**
     * It is upon the caller to close the channel
     * @param channel
     * @param <T>
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    // This is a bogus warning
    @SuppressWarnings("squid:MethodCyclomaticComplexity")
    public static <T extends CasualNetworkTransmittable> CasualNWMessage<T> read(final AsynchronousByteChannel channel)
    {
        final CompletableFuture<ByteBuffer> headerFuture = ByteUtils.readFully(channel, MessageHeaderSizes.getHeaderNetworkSize());
        final ByteBuffer headerBuffer;
        try
        {
            headerBuffer = headerFuture.get();
            final CasualNWMessageHeader header = CasualNetworkReader.networkHeaderToCasualHeader(headerBuffer.array());
            switch(header.getType())
            {
                case DOMAIN_DISCOVERY_REQUEST:
                    return readDomainDiscoveryRequest(channel, header);
                case DOMAIN_DISCOVERY_REPLY:
                    return readDomainDiscoveryReply(channel, header);
                case SERVICE_CALL_REQUEST:
                    return readServiceCallRequest(channel, header);
                case SERVICE_CALL_REPLY:
                    return readServiceCallReply(channel, header);
                case PREPARE_REQUEST:
                    return readTransactionPrepareRequest(channel, header);
                case PREPARE_REQUEST_REPLY:
                    return readTransactionPrepareReply(channel, header);
                case COMMIT_REQUEST:
                    return readTransactionCommitRequest(channel, header);
                case COMMIT_REQUEST_REPLY:
                    return readTransactionCommitReply(channel, header);
                case REQUEST_ROLLBACK:
                    return readTransactionRollbackRequest(channel, header);
                case REQUEST_ROLLBACK_REPLY:
                    return readTransactionRollbackReply(channel, header);
                default:
                    throw new UnsupportedOperationException("Unknown messagetype: " + header.getType());
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading", e);
        }
    }

    public static CasualNWMessageHeader networkHeaderToCasualHeader(final byte[] message)
    {
        return CasualNWMessageHeaderReader.fromNetworkBytes(message);
    }

    private static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readDomainDiscoveryRequest(final AsynchronousByteChannel channel, final CasualNWMessageHeader header )
    {
        final MessageReader<CasualDomainDiscoveryRequestMessage> reader = MessageReader.of(CasualDomainDiscoveryRequestMessageReader.of(), getMaxSingleBufferByteSize());
        final CasualDomainDiscoveryRequestMessage msg = reader.read(channel, header.getPayloadSize());
        return CasualNWMessage.of(header.getCorrelationId(), msg);
    }

    private static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readDomainDiscoveryReply(final AsynchronousByteChannel channel, final CasualNWMessageHeader header)
    {
        final MessageReader<CasualDomainDiscoveryReplyMessage> reader = MessageReader.of(CasualDomainDiscoveryReplyMessageReader.of(), getMaxSingleBufferByteSize());
        final CasualDomainDiscoveryReplyMessage msg = reader.read(channel, header.getPayloadSize());
        return CasualNWMessage.of(header.getCorrelationId(), msg);
    }

    private static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readServiceCallRequest(final AsynchronousByteChannel channel, final CasualNWMessageHeader header)
    {
        // We may want to use some other size for chunking of service payload
        CasualServiceCallRequestMessageReader.setMaxPayloadSingleBufferByteSize(getMaxSingleBufferByteSize());
        final MessageReader<CasualServiceCallRequestMessage> reader = MessageReader.of(CasualServiceCallRequestMessageReader.of(), getMaxSingleBufferByteSize());
        final CasualServiceCallRequestMessage msg = reader.read(channel, header.getPayloadSize());
        return CasualNWMessage.of(header.getCorrelationId(), msg);
    }

    private static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readServiceCallReply(final AsynchronousByteChannel channel, final CasualNWMessageHeader header)
    {
        // We may want to use some other size for chunking of service payload
        CasualServiceCallReplyMessageReader.setMaxPayloadSingleBufferByteSize(getMaxSingleBufferByteSize());
        final MessageReader<CasualServiceCallReplyMessage> reader = MessageReader.of(CasualServiceCallReplyMessageReader.of(), getMaxSingleBufferByteSize());
        final CasualServiceCallReplyMessage msg = reader.read(channel, header.getPayloadSize());
        return CasualNWMessage.of(header.getCorrelationId(), msg);
    }

    private static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readTransactionPrepareRequest(final AsynchronousByteChannel channel, final CasualNWMessageHeader header)
    {
        final MessageReader<CasualTransactionResourcePrepareRequestMessage> reader = MessageReader.of(CasualTransactionResourcePrepareRequestMessageReader.of(), getMaxSingleBufferByteSize());
        final CasualTransactionResourcePrepareRequestMessage msg = reader.read(channel, header.getPayloadSize());
        return CasualNWMessage.of(header.getCorrelationId(), msg);
    }

    private static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readTransactionPrepareReply(final AsynchronousByteChannel channel, final CasualNWMessageHeader header)
    {
        final MessageReader<CasualTransactionResourcePrepareReplyMessage> reader = MessageReader.of(CasualTransactionResourcePrepareReplyMessageReader.of(), getMaxSingleBufferByteSize());
        final CasualTransactionResourcePrepareReplyMessage msg = reader.read(channel, header.getPayloadSize());
        return CasualNWMessage.of(header.getCorrelationId(), msg);
    }

    private static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readTransactionCommitRequest(final AsynchronousByteChannel channel, final CasualNWMessageHeader header)
    {
        final MessageReader<CasualTransactionResourceCommitRequestMessage> reader = MessageReader.of(CasualTransactionResourceCommitRequestMessageReader.of(), getMaxSingleBufferByteSize());
        final CasualTransactionResourceCommitRequestMessage msg = reader.read(channel, header.getPayloadSize());
        return CasualNWMessage.of(header.getCorrelationId(), msg);
    }

    private static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readTransactionCommitReply(final AsynchronousByteChannel channel, final CasualNWMessageHeader header)
    {
        final MessageReader<CasualTransactionResourceCommitReplyMessage> reader = MessageReader.of(CasualTransactionResourceCommitReplyMessageReader.of(), getMaxSingleBufferByteSize());
        final CasualTransactionResourceCommitReplyMessage msg = reader.read(channel, header.getPayloadSize());
        return CasualNWMessage.of(header.getCorrelationId(), msg);
    }

    private static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readTransactionRollbackRequest(final AsynchronousByteChannel channel, final CasualNWMessageHeader header)
    {
        final MessageReader<CasualTransactionResourceRollbackRequestMessage> reader = MessageReader.of(CasualTransactionResourceRollbackRequestMessageReader.of(), getMaxSingleBufferByteSize());
        final CasualTransactionResourceRollbackRequestMessage msg = reader.read(channel, header.getPayloadSize());
        return CasualNWMessage.of(header.getCorrelationId(), msg);
    }

    private static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readTransactionRollbackReply(final AsynchronousByteChannel channel, final CasualNWMessageHeader header)
    {
        final MessageReader<CasualTransactionResourceRollbackReplyMessage> reader = MessageReader.of(CasualTransactionResourceRollbackReplyMessageReader.of(), getMaxSingleBufferByteSize());
        final CasualTransactionResourceRollbackReplyMessage msg = reader.read(channel, header.getPayloadSize());
        return CasualNWMessage.of(header.getCorrelationId(), msg);
    }

}
