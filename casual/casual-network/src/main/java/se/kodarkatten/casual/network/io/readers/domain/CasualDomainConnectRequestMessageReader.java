package se.kodarkatten.casual.network.io.readers.domain;

import se.kodarkatten.casual.network.io.readers.NetworkReader;
import se.kodarkatten.casual.network.io.readers.utils.CasualNetworkReaderUtils;
import se.kodarkatten.casual.network.messages.domain.CasualDomainConnectRequestMessage;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.messages.parseinfo.ConnectRequestSizes;
import se.kodarkatten.casual.network.utils.ByteUtils;

import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class CasualDomainConnectRequestMessageReader implements NetworkReader<CasualDomainConnectRequestMessage>
{
    private CasualDomainConnectRequestMessageReader()
    {}

    public static NetworkReader<CasualDomainConnectRequestMessage> of()
    {
        return new CasualDomainConnectRequestMessageReader();
    }

    @Override
    public CasualDomainConnectRequestMessage readSingleBuffer(final AsynchronousByteChannel channel, int messageSize)
    {
        final CompletableFuture<ByteBuffer> msgFuture = ByteUtils.readFully(channel, messageSize);
        try
        {
            return getMessage(msgFuture.get().array());
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading CasualDomainDiscoveryRequestMessage", e);
        }
    }

    @Override
    public CasualDomainConnectRequestMessage readChunked(final AsynchronousByteChannel channel)
    {
        try
        {
            final UUID execution = CasualNetworkReaderUtils.readUUID(channel);
            final UUID domainId = CasualNetworkReaderUtils.readUUID(channel);
            final int domainNameSize = (int) ByteUtils.readFully(channel, ConnectRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize()).get().getLong();
            final String domainName = CasualNetworkReaderUtils.readString(channel, domainNameSize);
            final List<Long> protocols = readProtocols(channel);
            return CasualDomainConnectRequestMessage.createBuilder()
                                                    .withExecution(execution)
                                                    .withDomainId(domainId)
                                                    .withDomainName(domainName)
                                                    .withProtocols(protocols)
                                                    .build();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading CasualServiceCallRequestMessage", e);
        }
    }

    @Override
    public CasualDomainConnectRequestMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        return getMessage(b.array());
    }

    @Override
    public CasualDomainConnectRequestMessage readChunked(final ReadableByteChannel channel)
    {
        final UUID execution = CasualNetworkReaderUtils.readUUID(channel);
        final UUID domainId = CasualNetworkReaderUtils.readUUID(channel);
        final int domainNameSize = (int) ByteUtils.readFully(channel, ConnectRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        final String domainName = CasualNetworkReaderUtils.readString(channel, domainNameSize);
        final List<Long> protocols = readProtocols(channel);
        return CasualDomainConnectRequestMessage.createBuilder()
                                                .withExecution(execution)
                                                .withDomainId(domainId)
                                                .withDomainName(domainName)
                                                .withProtocols(protocols)
                                                .build();
    }

    private List<Long> readProtocols(final AsynchronousByteChannel channel)
    {
        try
        {
            long numberOfProtocols = ByteUtils.readFully(channel, ConnectRequestSizes.PROTOCOL_VERSION_SIZE.getNetworkSize()).get().getLong();
            List<Long> l = new ArrayList<>();
            for(; numberOfProtocols > 0; --numberOfProtocols)
            {
                Long version = ByteUtils.readFully(channel, ConnectRequestSizes.PROTOCOL_ELEMENT_SIZE.getNetworkSize()).get().getLong();
                l.add(version);
            }
            return l;
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed when reading protocols for CasualServiceCallRequestMessage", e);
        }
    }

    private List<Long> readProtocols(final ReadableByteChannel channel)
    {
        long numberOfProtocols = ByteUtils.readFully(channel, ConnectRequestSizes.PROTOCOL_VERSION_SIZE.getNetworkSize()).getLong();
        List<Long> l = new ArrayList<>();
        for(; numberOfProtocols > 0; --numberOfProtocols)
        {
            Long version = ByteUtils.readFully(channel, ConnectRequestSizes.PROTOCOL_ELEMENT_SIZE.getNetworkSize()).getLong();
            l.add(version);
        }
        return l;
    }

    private CasualDomainConnectRequestMessage getMessage(final byte[] bytes)
    {
        int currentOffset = 0;
        final UUID execution = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, ConnectRequestSizes.EXECUTION.getNetworkSize()));
        currentOffset +=  ConnectRequestSizes.EXECUTION.getNetworkSize();
        final UUID domainId = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, currentOffset + ConnectRequestSizes.DOMAIN_ID.getNetworkSize()));
        currentOffset += ConnectRequestSizes.DOMAIN_ID.getNetworkSize();
        final int domainNameSize = (int)ByteBuffer.wrap(bytes, currentOffset , ConnectRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ConnectRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize();
        final String domainName = CasualNetworkReaderUtils.getAsString(bytes, currentOffset, domainNameSize);
        currentOffset += domainNameSize;
        long numberOfProtocols = ByteBuffer.wrap(bytes, currentOffset , ConnectRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ConnectRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize();
        List<Long> protocols = new ArrayList<>();
        for(; numberOfProtocols > 0; --numberOfProtocols)
        {
            Long version = ByteBuffer.wrap(bytes, currentOffset, ConnectRequestSizes.PROTOCOL_ELEMENT_SIZE.getNetworkSize()).getLong();
            protocols.add(version);
            currentOffset += ConnectRequestSizes.PROTOCOL_ELEMENT_SIZE.getNetworkSize();
        }
        return CasualDomainConnectRequestMessage.createBuilder()
                                                .withExecution(execution)
                                                .withDomainId(domainId)
                                                .withDomainName(domainName)
                                                .withProtocols(protocols)
                                                .build();
    }

}
