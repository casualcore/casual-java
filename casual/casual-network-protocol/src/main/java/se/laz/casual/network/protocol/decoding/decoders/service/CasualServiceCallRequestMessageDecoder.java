/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.service;

import se.laz.casual.api.buffer.CasualHeaders;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.util.Pair;
import se.laz.casual.jca.SpanId;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.decoding.decoders.utils.DecoderReaderValidator;
import se.laz.casual.network.protocol.decoding.decoders.utils.HeaderDecoder;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.messages.parseinfo.ServiceCallRequestSizes;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static se.laz.casual.network.ProtocolVersion.VERSION_1_3;

public final class CasualServiceCallRequestMessageDecoder implements NetworkDecoder<CasualServiceCallRequestMessage>
{
    private static int maxPayloadSingleBufferByteSize = Integer.MAX_VALUE;
    private final ProtocolVersion protocolVersion;

    private CasualServiceCallRequestMessageDecoder(ProtocolVersion protocolVersion)
    {
        this.protocolVersion = protocolVersion;
    }

    public static NetworkDecoder<CasualServiceCallRequestMessage> of(ProtocolVersion protocolVersion)
    {
        Objects.requireNonNull(protocolVersion, "protocolVersion can not be null");
        return new CasualServiceCallRequestMessageDecoder(protocolVersion);
    }

    /**
     * Number of maximum bytes before any chunk reading takes place
     * Defaults to Integer.MAX_VALUE
     * @return maximum number of bytes for a single buffer payload.
     */
    public static int getMaxPayloadSingleBufferByteSize()
    {
        return maxPayloadSingleBufferByteSize;
    }

    /**
     * If not set, defaults to Integer.MAX_VALUE
     * Can be used in testing to force chunked reading
     * by for instance setting it to 1
     * @param maxPayloadSingleBufferByteSize maximum number of bytes for a single buffer payload.
     */
    public static void setMaxPayloadSingleBufferByteSize(int maxPayloadSingleBufferByteSize)
    {
        CasualServiceCallRequestMessageDecoder.maxPayloadSingleBufferByteSize = maxPayloadSingleBufferByteSize;
    }

    @Override
    public CasualServiceCallRequestMessage readSingleBuffer(byte[] data)
    {
        return createMessage(data);
    }

    private CasualServiceCallRequestMessage createMessage(final byte[] data)
    {
        int currentOffset = 0;
        final UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(data, currentOffset, CommonSizes.EXECUTION.getNetworkSize()));
        currentOffset += CommonSizes.EXECUTION.getNetworkSize();

        int serviceNameLen = (int)ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.SERVICE_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.SERVICE_NAME_SIZE.getNetworkSize();
        final String serviceName = CasualMessageDecoderUtils.getAsString(data, currentOffset, serviceNameLen);
        currentOffset += serviceNameLen;

        boolean hasTimeout = true;
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ))
        {
            byte value = ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.HAS_VALUE.getNetworkSize()).get();
            currentOffset += ServiceCallRequestSizes.HAS_VALUE.getNetworkSize();
            hasTimeout = (value > 0);
        }
        long timeout = 0;
        if(hasTimeout)
        {
            timeout = ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.SERVICE_TIMEOUT.getNetworkSize()).getLong();
            currentOffset += ServiceCallRequestSizes.SERVICE_TIMEOUT.getNetworkSize();
        }
        byte[] parentSpan = null;
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ))
        {
            parentSpan = new byte[ServiceCallRequestSizes.PARENT_SPAN.getNetworkSize()];
            System.arraycopy(data, currentOffset, parentSpan, 0, ServiceCallRequestSizes.PARENT_SPAN.getNetworkSize());
            currentOffset += ServiceCallRequestSizes.PARENT_SPAN.getNetworkSize();
        }
        final int parentNameSize = (int)ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.PARENT_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.PARENT_NAME_SIZE.getNetworkSize();
        final String parentName = CasualMessageDecoderUtils.getAsString(data, currentOffset, parentNameSize);
        currentOffset += parentNameSize;

        Pair<Integer, Xid> xidInfo = CasualMessageDecoderUtils.readXid(data, currentOffset);
        currentOffset = xidInfo.first();
        final Xid xid = xidInfo.second();

        int flags = (int) ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.FLAGS.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.FLAGS.getNetworkSize();
        int serviceBufferTypeSize = (int) ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize();
        final String serviceTypeName = CasualMessageDecoderUtils.getAsString(data, currentOffset, serviceBufferTypeSize);
        currentOffset += serviceBufferTypeSize;
        // this can be huge, ie not fitting into one ByteBuffer
        // but since the whole message fits into Integer.MAX_VALUE that is not true of this message
        int serviceBufferPayloadSize = (int) ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize();
        final byte[] payloadData = Arrays.copyOfRange(data, currentOffset, currentOffset + serviceBufferPayloadSize);
        final List<byte[]> serviceBufferPayload = new ArrayList<>();
        serviceBufferPayload.add(payloadData);
        final ServiceBuffer serviceBuffer = ServiceBuffer.of(serviceTypeName, serviceBufferPayload);

        currentOffset += serviceBufferPayloadSize;

        CasualServiceCallRequestMessage.Builder builder = CasualServiceCallRequestMessage.createBuilder()
                                                                                         .setExecution(execution)
                                                                                         .setServiceName(serviceName)
                                                                                         .setParentName(parentName)
                                                                                         .setProtocolVersion(protocolVersion)
                                                                                         .setXid(xid)
                                                                                         .setXatmiFlags(new Flag.Builder<AtmiFlags>(flags).build())
                                                                                         .setServiceBuffer(serviceBuffer);

        if( protocolVersion.isGreaterThanOrEqualTo( ProtocolVersion.VERSION_1_5 ) )
        {
            List<String> rawHeaders = new ArrayList<>();

            currentOffset = HeaderDecoder.decodeHeaders( data, currentOffset, rawHeaders );
            CasualHeaders headers = CasualHeaders.newBuilder().addAll( rawHeaders ).build();

            builder.setHeaders( headers );
        }


        if(hasTimeout)
        {
            builder.setTimeout(timeout);
        }
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ))
        {
            builder.setParentSpan(SpanId.of(parentSpan));
        }

        DecoderReaderValidator.throwIfDataNotFullyRead( currentOffset, data.length );

        return builder.build();
    }

}
