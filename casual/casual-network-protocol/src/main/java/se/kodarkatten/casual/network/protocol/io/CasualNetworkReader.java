package se.kodarkatten.casual.network.protocol.io;


import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage;

import se.kodarkatten.casual.network.protocol.io.readers.CasualNWMessageHeaderReader;
import se.kodarkatten.casual.network.protocol.io.readers.MessageReader;
import se.kodarkatten.casual.network.protocol.io.readers.NetworkReader;
import se.kodarkatten.casual.network.protocol.io.readers.domain.CasualDomainConnectReplyMessageReader;
import se.kodarkatten.casual.network.protocol.io.readers.domain.CasualDomainConnectRequestMessageReader;
import se.kodarkatten.casual.network.protocol.io.readers.domain.CasualDomainDiscoveryReplyMessageReader;
import se.kodarkatten.casual.network.protocol.io.readers.domain.CasualDomainDiscoveryRequestMessageReader;
import se.kodarkatten.casual.network.protocol.io.readers.queue.CasualDequeueReplyMessageReader;
import se.kodarkatten.casual.network.protocol.io.readers.queue.CasualDequeueRequestMessageReader;
import se.kodarkatten.casual.network.protocol.io.readers.queue.CasualEnqueueReplyMessageReader;
import se.kodarkatten.casual.network.protocol.io.readers.queue.CasualEnqueueRequestMessageReader;
import se.kodarkatten.casual.network.protocol.io.readers.service.CasualServiceCallReplyMessageReader;
import se.kodarkatten.casual.network.protocol.io.readers.service.CasualServiceCallRequestMessageReader;
import se.kodarkatten.casual.network.protocol.io.readers.transaction.CasualTransactionResourceCommitReplyMessageReader;
import se.kodarkatten.casual.network.protocol.io.readers.transaction.CasualTransactionResourceCommitRequestMessageReader;
import se.kodarkatten.casual.network.protocol.io.readers.transaction.CasualTransactionResourcePrepareReplyMessageReader;
import se.kodarkatten.casual.network.protocol.io.readers.transaction.CasualTransactionResourcePrepareRequestMessageReader;
import se.kodarkatten.casual.network.protocol.io.readers.transaction.CasualTransactionResourceRollbackReplyMessageReader;
import se.kodarkatten.casual.network.protocol.io.readers.transaction.CasualTransactionResourceRollbackRequestMessageReader;
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl;
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageHeader;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.network.protocol.messages.exceptions.CasualProtocolException;
import se.kodarkatten.casual.network.protocol.messages.parseinfo.MessageHeaderSizes;
import se.kodarkatten.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.ReadableByteChannel;
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
            NetworkReader<T> networkReader = getNetworkReader( header );
            return readMessage( channel, header, networkReader );
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualProtocolException("failed reading", e);
        }
    }

    public static <T extends CasualNetworkTransmittable> CasualNWMessage<T> read(final ReadableByteChannel channel, CasualNWMessageHeader header )
    {
        NetworkReader<T> networkReader = getNetworkReader( header );

        return readMessage( channel, header, networkReader );
    }

    public static <T extends CasualNetworkTransmittable> CasualNWMessage<T> read(final ReadableByteChannel channel)
    {
        final CasualNWMessageHeader header = networkHeaderToCasualHeader( channel );

        return read( channel, header );
    }

    /**
     * Lock the channel for read and then read an entire message.
     *
     * Blocks until read lock is acquired and then until message is read fully.
     *
     * @param channel to read from
     * @param <T> of the message to return.
     * @return message read from the channel.
     */
    public static <T extends CasualNetworkTransmittable> CasualNWMessage<T> read(final LockableSocketChannel channel )
    {
        try
        {
            channel.lockRead();
            return read( channel.getSocketChannel() );
        }
        finally
        {
            channel.unlockRead();
        }
    }

    public static CasualNWMessageHeader networkHeaderToCasualHeader( final ReadableByteChannel channel )
    {
        final ByteBuffer headerBuffer = ByteUtils.readFully(channel, MessageHeaderSizes.getHeaderNetworkSize());
        return CasualNetworkReader.networkHeaderToCasualHeader(headerBuffer.array());
    }

    public static CasualNWMessageHeader networkHeaderToCasualHeader(final byte[] message)
    {
        return CasualNWMessageHeaderReader.fromNetworkBytes(message);
    }

    public static <T extends CasualNetworkTransmittable> CasualNWMessage<T> read(final byte[] data, CasualNWMessageHeader header)
    {
        NetworkReader<T> networkReader = getNetworkReader( header );
        return readMessage( data, header, networkReader );
    }

    @SuppressWarnings({"unchecked", "squid:MethodCyclomaticComplexity"})
    private static <T extends CasualNetworkTransmittable> NetworkReader<T> getNetworkReader( CasualNWMessageHeader header )
    {
        switch(header.getType())
        {
            case DOMAIN_DISCOVERY_REQUEST:
                return (NetworkReader<T>)CasualDomainDiscoveryRequestMessageReader.of();
            case DOMAIN_DISCOVERY_REPLY:
                return (NetworkReader<T>)CasualDomainDiscoveryReplyMessageReader.of();
            case DOMAIN_CONNECT_REQUEST:
                return (NetworkReader<T>)CasualDomainConnectRequestMessageReader.of();
            case DOMAIN_CONNECT_REPLY:
                return (NetworkReader<T>)CasualDomainConnectReplyMessageReader.of();
            case SERVICE_CALL_REQUEST:
                // We may want to use some other size for chunking of service payload
                CasualServiceCallRequestMessageReader.setMaxPayloadSingleBufferByteSize(getMaxSingleBufferByteSize());
                return (NetworkReader<T>)CasualServiceCallRequestMessageReader.of();
            case SERVICE_CALL_REPLY:
                // We may want to use some other size for chunking of service payload
                CasualServiceCallReplyMessageReader.setMaxPayloadSingleBufferByteSize(getMaxSingleBufferByteSize());
                return (NetworkReader<T>)CasualServiceCallReplyMessageReader.of();
            case ENQUEUE_REQUEST:
                return (NetworkReader<T>)CasualEnqueueRequestMessageReader.of();
            case ENQUEUE_REPLY:
                return (NetworkReader<T>)CasualEnqueueReplyMessageReader.of();
            case DEQUEUE_REQUEST:
                return (NetworkReader<T>) CasualDequeueRequestMessageReader.of();
            case DEQUEUE_REPLY:
                return (NetworkReader<T>) CasualDequeueReplyMessageReader.of();
            case PREPARE_REQUEST:
                return (NetworkReader<T>)CasualTransactionResourcePrepareRequestMessageReader.of();
            case PREPARE_REQUEST_REPLY:
                return (NetworkReader<T>)CasualTransactionResourcePrepareReplyMessageReader.of();
            case COMMIT_REQUEST:
                return (NetworkReader<T>)CasualTransactionResourceCommitRequestMessageReader.of();
            case COMMIT_REQUEST_REPLY:
                return (NetworkReader<T>)CasualTransactionResourceCommitReplyMessageReader.of();
            case REQUEST_ROLLBACK:
                return (NetworkReader<T>)CasualTransactionResourceRollbackRequestMessageReader.of();
            case REQUEST_ROLLBACK_REPLY:
                return (NetworkReader<T>)CasualTransactionResourceRollbackReplyMessageReader.of();
            default:
                throw new UnsupportedOperationException("Unknown messagetype: " + header.getType());
        }
    }

    private static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readMessage(final byte[] data, final CasualNWMessageHeader header, NetworkReader<T> nr )
    {
        final MessageReader<T> reader = MessageReader.of(nr, getMaxSingleBufferByteSize() );
        final T msg = reader.read(data);
        return CasualNWMessageImpl.of(header.getCorrelationId(), msg);
    }

    //sync
    private static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readMessage(final ReadableByteChannel channel, final CasualNWMessageHeader header, NetworkReader<T> nr )
    {
        final MessageReader<T> reader = MessageReader.of(nr, getMaxSingleBufferByteSize() );
        final T msg = reader.read(channel, header.getPayloadSize());
        return CasualNWMessageImpl.of(header.getCorrelationId(), msg);
    }


    // async
    private static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readMessage(final AsynchronousByteChannel channel, final CasualNWMessageHeader header, NetworkReader<T> nr )
    {
        final MessageReader<T> reader = MessageReader.of(nr, getMaxSingleBufferByteSize());
        final T msg = reader.read(channel, header.getPayloadSize());
        return CasualNWMessageImpl.of(header.getCorrelationId(), msg);
    }
}
