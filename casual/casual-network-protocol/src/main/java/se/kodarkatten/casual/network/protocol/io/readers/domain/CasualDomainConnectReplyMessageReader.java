package se.kodarkatten.casual.network.protocol.io.readers.domain;

import se.kodarkatten.casual.network.protocol.io.readers.NetworkReader;
import se.kodarkatten.casual.network.protocol.io.readers.utils.CasualNetworkReaderUtils;
import se.kodarkatten.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage;
import se.kodarkatten.casual.network.protocol.messages.exceptions.CasualProtocolException;
import se.kodarkatten.casual.network.protocol.messages.parseinfo.ConnectReplySizes;
import se.kodarkatten.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class CasualDomainConnectReplyMessageReader implements NetworkReader<CasualDomainConnectReplyMessage>
{
    private CasualDomainConnectReplyMessageReader()
    {}

    public static NetworkReader<CasualDomainConnectReplyMessage> of()
    {
        return new CasualDomainConnectReplyMessageReader();
    }

    @Override
    public CasualDomainConnectReplyMessage readSingleBuffer(final AsynchronousByteChannel channel, int messageSize)
    {
        final CompletableFuture<ByteBuffer> msgFuture = ByteUtils.readFully(channel, messageSize);
        try
        {
            return getMessage(msgFuture.get().array());
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualProtocolException("failed reading CasualDomainConnectReplyMessage", e);
        }
    }

    @Override
    public CasualDomainConnectReplyMessage readChunked(final AsynchronousByteChannel channel)
    {
        try
        {
            final UUID execution = CasualNetworkReaderUtils.readUUID(channel);
            final UUID domainId = CasualNetworkReaderUtils.readUUID(channel);
            final int domainNameSize = (int) ByteUtils.readFully(channel, ConnectReplySizes.DOMAIN_NAME_SIZE.getNetworkSize()).get().getLong();
            final String domainName = CasualNetworkReaderUtils.readString(channel, domainNameSize);
            final long protocol = ByteUtils.readFully(channel, ConnectReplySizes.PROTOCOL_VERSION_SIZE.getNetworkSize()).get().getLong();
            return CasualDomainConnectReplyMessage.createBuilder()
                                                  .withExecution(execution)
                                                  .withDomainId(domainId)
                                                  .withDomainName(domainName)
                                                  .withProtocolVersion(protocol)
                                                  .build();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualProtocolException("failed reading CasualServiceCallReplyMessage", e);
        }
    }

    @Override
    public CasualDomainConnectReplyMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        return getMessage(b.array());
    }

    @Override
    public CasualDomainConnectReplyMessage readChunked(final ReadableByteChannel channel)
    {
        final UUID execution = CasualNetworkReaderUtils.readUUID(channel);
        final UUID domainId = CasualNetworkReaderUtils.readUUID(channel);
        final int domainNameSize = (int) ByteUtils.readFully(channel, ConnectReplySizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        final String domainName = CasualNetworkReaderUtils.readString(channel, domainNameSize);
        final long protocol = ByteUtils.readFully(channel, ConnectReplySizes.PROTOCOL_VERSION_SIZE.getNetworkSize()).getLong();
        return CasualDomainConnectReplyMessage.createBuilder()
                                              .withExecution(execution)
                                              .withDomainId(domainId)
                                              .withDomainName(domainName)
                                              .withProtocolVersion(protocol)
                                              .build();
    }

    @Override
    public CasualDomainConnectReplyMessage readSingleBuffer(byte[] data)
    {
        return getMessage(data);
    }

    private CasualDomainConnectReplyMessage getMessage(final byte[] bytes)
    {
        int currentOffset = 0;
        final UUID execution = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, ConnectReplySizes.EXECUTION.getNetworkSize()));
        currentOffset +=  ConnectReplySizes.EXECUTION.getNetworkSize();
        final UUID domainId = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, currentOffset + ConnectReplySizes.DOMAIN_ID.getNetworkSize()));
        currentOffset += ConnectReplySizes.DOMAIN_ID.getNetworkSize();
        final int domainNameSize = (int)ByteBuffer.wrap(bytes, currentOffset , ConnectReplySizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ConnectReplySizes.DOMAIN_NAME_SIZE.getNetworkSize();
        final String domainName = CasualNetworkReaderUtils.getAsString(bytes, currentOffset, domainNameSize);
        currentOffset += domainNameSize;
        long protocol = ByteBuffer.wrap(bytes, currentOffset, ConnectReplySizes.PROTOCOL_VERSION_SIZE.getNetworkSize()).getLong();
        return CasualDomainConnectReplyMessage.createBuilder()
                                              .withExecution(execution)
                                              .withDomainId(domainId)
                                              .withDomainName(domainName)
                                              .withProtocolVersion(protocol)
                                              .build();
    }

}
