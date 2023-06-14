/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.domain;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.laz.casual.network.protocol.messages.parseinfo.DiscoveryRequestSizes;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by aleph on 2017-03-02.
 */
/**
 * sonar hates lambdas...
 * It should pick up
 * sourceCompatibility = "1.8"
 * targetCompatibility = "1.8"
 * but it seems it does not
 **/
public final class CasualDomainDiscoveryRequestMessageDecoder implements NetworkDecoder<CasualDomainDiscoveryRequestMessage>
{
    private CasualDomainDiscoveryRequestMessageDecoder()
    {}

    public static NetworkDecoder<CasualDomainDiscoveryRequestMessage> of()
    {
        return new CasualDomainDiscoveryRequestMessageDecoder();
    }

    @Override
    public CasualDomainDiscoveryRequestMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        ByteBuf buffer = Unpooled.wrappedBuffer(b.array());
        CasualDomainDiscoveryRequestMessage msg = getMessage(buffer);
        buffer.release();
        return msg;
    }

    @Override
    public CasualDomainDiscoveryRequestMessage readChunked(final ReadableByteChannel channel)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(channel);
        final UUID domainId = CasualMessageDecoderUtils.readUUID(channel);
        final int domainNameSize = (int) ByteUtils.readFully(channel, DiscoveryRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        final String domainName = CasualMessageDecoderUtils.readString(channel, domainNameSize);
        final List<String> services = readServices(channel);
        final List<String> queues = readQueues(channel);
        return CasualDomainDiscoveryRequestMessage.createBuilder()
                                                  .setExecution(execution)
                                                  .setDomainId(domainId)
                                                  .setDomainName(domainName)
                                                  .setServiceNames(services)
                                                  .setQueueNames(queues)
                                                  .build();
    }

    @Override
    public CasualDomainDiscoveryRequestMessage readSingleBuffer(final ByteBuf buffer)
    {
        return getMessage(buffer);
    }

    public static CasualDomainDiscoveryRequestMessage getMessage(final ByteBuf buffer)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(buffer);
        final UUID domainId = CasualMessageDecoderUtils.readUUID(buffer);
        final int domainNameSize = (int)buffer.readLong();
        final String domainName = CasualMessageDecoderUtils.readAsString(buffer, domainNameSize);
        List<String> serviceNames = readNames(buffer);
        List<String> queueNames = readNames(buffer);
        return CasualDomainDiscoveryRequestMessage.createBuilder()
                                                  .setExecution(execution)
                                                  .setDomainId(domainId)
                                                  .setDomainName(domainName)
                                                  .setServiceNames(serviceNames)
                                                  .setQueueNames(queueNames)
                                                  .build();
    }

    private static List<String> readNames(ByteBuf buffer)
    {
        long numberOfNames = buffer.readLong();
        List<String> names = new ArrayList<>();
        for(int i = 0; i < numberOfNames; ++i)
        {
            int nameLength = (int)buffer.readLong();
            names.add(buffer.readBytes(nameLength).toString(StandardCharsets.UTF_8));
        }
        return names;
    }

    private static List<String> readQueues(final ReadableByteChannel channel)
    {
        final List<String> queues = new ArrayList<>();
        final long numberOfQueues = ByteUtils.readFully(channel, DiscoveryRequestSizes.QUEUES_SIZE.getNetworkSize()).getLong();
        for(int i = 0; i < numberOfQueues; ++i)
        {
            queues.add(CasualMessageDecoderUtils.readString(channel));
        }
        return queues;
    }

    private static List<String> readServices(final ReadableByteChannel channel)
    {
        final long numberOfServices = ByteUtils.readFully(channel, DiscoveryRequestSizes.SERVICES_SIZE.getNetworkSize()).getLong();
        final List<String> services = new ArrayList<>();
        for(int i = 0; i < numberOfServices; ++i)
        {
            services.add(CasualMessageDecoderUtils.readString(channel));
        }
        return services;
    }

}
