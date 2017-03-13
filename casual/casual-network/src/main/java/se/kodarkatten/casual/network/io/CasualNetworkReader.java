package se.kodarkatten.casual.network.io;


import se.kodarkatten.casual.network.io.readers.CasualDomainDiscoveryReplyMessageReader;
import se.kodarkatten.casual.network.io.readers.CasualDomainDiscoveryRequestMessageReader;
import se.kodarkatten.casual.network.io.readers.CasualNWMessageHeaderReader;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.CasualNWMessageHeader;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.messages.parseinfo.MessageHeaderSizes;
import se.kodarkatten.casual.network.messages.reply.CasualDomainDiscoveryReplyMessage;
import se.kodarkatten.casual.network.messages.request.CasualDomainDiscoveryRequestMessage;
import se.kodarkatten.casual.network.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.util.List;
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
                default:
                    throw new UnsupportedOperationException("reading of messagetype: " + header.getType() + " is not implemented yet");
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

    /**
     * When reading the bytes - if the payload in the header is less than Integer.MAX_VALUE
     * then this list of bytes should only contain one byte[]
     * If not, list should contain one byte[] per member of
     * message::interdomain::domain::discovery::Request that contain dynamic arrays
     * Other data may be packed into one byte[] but the format is not yet settled
     * @param payload
     * @return
     */
    public static CasualDomainDiscoveryRequestMessage networkDomainDiscoveryRequestToCasualDomainDiscoveryRequestMessage(final List<byte[]> payload)
    {
        return CasualDomainDiscoveryRequestMessageReader.fromNetworkBytes(payload);
    }

    public static CasualDomainDiscoveryReplyMessage networkDomainDiscoverReplyToCasualDomainDiscoveryReplyMessage(final List<byte[]> payload)
    {
        return CasualDomainDiscoveryReplyMessageReader.fromNetworkBytes(payload);
    }

    private static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readDomainDiscoveryRequest(final AsynchronousByteChannel channel, final CasualNWMessageHeader header ) throws ExecutionException, InterruptedException
    {
        CasualDomainDiscoveryRequestMessageReader.setMaxSingleBufferByteSize(getMaxSingleBufferByteSize());
        final CasualDomainDiscoveryRequestMessage msg = CasualDomainDiscoveryRequestMessageReader.read(channel, header.getPayloadSize());
        return CasualNWMessage.of(header.getCorrelationId(), msg);
    }

    private static <T extends CasualNetworkTransmittable> CasualNWMessage<T> readDomainDiscoveryReply(AsynchronousByteChannel channel, CasualNWMessageHeader header) throws ExecutionException, InterruptedException
    {
        CasualDomainDiscoveryReplyMessageReader.setMaxSingleBufferByteSize(getMaxSingleBufferByteSize());
        final CasualDomainDiscoveryReplyMessage msg = CasualDomainDiscoveryReplyMessageReader.read(channel, header.getPayloadSize());
        return CasualNWMessage.of(header.getCorrelationId(), msg);
    }

}
