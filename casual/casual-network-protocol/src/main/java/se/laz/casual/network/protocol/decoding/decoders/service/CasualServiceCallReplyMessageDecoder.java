/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.service;

import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.TransactionState;
import se.laz.casual.api.util.Pair;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.ServiceCallReplySizes;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static se.laz.casual.network.ProtocolVersion.VERSION_1_3;

/**
 * Created by aleph on 2017-03-28.
 */
public final class CasualServiceCallReplyMessageDecoder implements NetworkDecoder<CasualServiceCallReplyMessage>
{
    private final ProtocolVersion protocolVersion;
    private CasualServiceCallReplyMessageDecoder(ProtocolVersion protocolVersion)
    {
        this.protocolVersion = protocolVersion;
    }

    public static NetworkDecoder<CasualServiceCallReplyMessage> of(ProtocolVersion protocolVersion)
    {
        Objects.requireNonNull(protocolVersion, "protocol version can not be null");
        return new CasualServiceCallReplyMessageDecoder(protocolVersion);
    }

    @Override
    public CasualServiceCallReplyMessage readSingleBuffer(byte[] data)
    {
        return createMessage(data);
    }

    private CasualServiceCallReplyMessage createMessage(final byte[] data)
    {
        int currentOffset = 0;
        final UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(data, currentOffset, ServiceCallReplySizes.EXECUTION.getNetworkSize()));
        currentOffset += ServiceCallReplySizes.EXECUTION.getNetworkSize();

        final ByteBuffer callErrorBuffer = ByteBuffer.wrap(data, currentOffset, ServiceCallReplySizes.CALL_ERROR.getNetworkSize());
        int callError = callErrorBuffer.getInt();
        currentOffset += ServiceCallReplySizes.CALL_ERROR.getNetworkSize();

        final ByteBuffer userErrorBuffer = ByteBuffer.wrap(data, currentOffset, ServiceCallReplySizes.CALL_CODE.getNetworkSize());
        long userError = userErrorBuffer.getLong();
        currentOffset += ServiceCallReplySizes.CALL_CODE.getNetworkSize();

        Xid xid = null;
        if(protocolVersion.isLessThan( VERSION_1_3 ) )
        {
            Pair<Integer, Xid> xidInfo = CasualMessageDecoderUtils.readXid(data, currentOffset);
            currentOffset = xidInfo.first();
            xid = xidInfo.second();
        }

        final ByteBuffer transactionStateBuffer = ByteBuffer.wrap(data, currentOffset, ServiceCallReplySizes.TRANSACTION_STATE.getNetworkSize());
        int transactionState = transactionStateBuffer.get();
        currentOffset += ServiceCallReplySizes.TRANSACTION_STATE.getNetworkSize();

        int serviceBufferTypeSize = (int) ByteBuffer.wrap(data, currentOffset, ServiceCallReplySizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ServiceCallReplySizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize();
        String serviceTypeName = ( 0 == serviceBufferTypeSize) ? "" : CasualMessageDecoderUtils.getAsString(data, currentOffset, serviceBufferTypeSize);
        currentOffset += serviceBufferTypeSize;
        // this can be huge, ie not fitting into one ByteBuffer
        // but since the whole message fits into Integer.MAX_VALUE that is not true of this message
        // The payload may also not exist at all in the reply message
        // If so, then the typename does not exist either
        // This could happen for instance on TPESVCERR
        int serviceBufferPayloadSize = (int) ByteBuffer.wrap(data, currentOffset, ServiceCallReplySizes.BUFFER_PAYLOAD_SIZE.getNetworkSize()).getLong();
        currentOffset += ServiceCallReplySizes.BUFFER_PAYLOAD_SIZE.getNetworkSize();

        final List<byte[]> serviceBufferPayload = new ArrayList<>();
        if(serviceBufferPayloadSize > 0)
        {
            final byte[] payloadData = Arrays.copyOfRange(data, currentOffset, currentOffset + serviceBufferPayloadSize);
            serviceBufferPayload.add(payloadData);
        }
        // since serviceTypeName can be NULL in case there is no payload
        serviceTypeName = (0 == serviceBufferPayloadSize) ? "" : serviceTypeName;
        final ServiceBuffer serviceBuffer = ServiceBuffer.of(serviceTypeName, serviceBufferPayload);
        CasualServiceCallReplyMessage.Builder builder = CasualServiceCallReplyMessage.createBuilder()
                                                                                     .setExecution(execution)
                                                                                     .setError(ErrorState.unmarshal(callError))
                                                                                     .setUserSuppliedError(userError)
                                                                                     .setTransactionState(TransactionState.unmarshal(transactionState))
                                                                                     .setServiceBuffer(serviceBuffer)
                                                                                     .setProtocolVersion(protocolVersion);
        if(null != xid)
        {
            builder.setXid(xid);
        }
        return builder.build();
    }
}
