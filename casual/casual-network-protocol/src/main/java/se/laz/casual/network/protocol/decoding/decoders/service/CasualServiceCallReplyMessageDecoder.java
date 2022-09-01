/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.service;

import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.TransactionState;
import se.laz.casual.api.util.Pair;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.ServiceCallReplySizes;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.network.protocol.utils.ByteUtils;
import se.laz.casual.network.protocol.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-28.
 */
public final class CasualServiceCallReplyMessageDecoder implements NetworkDecoder<CasualServiceCallReplyMessage>
{
    private static int maxPayloadSingleBufferByteSize = Integer.MAX_VALUE;
    private CasualServiceCallReplyMessageDecoder()
    {}

    public static NetworkDecoder<CasualServiceCallReplyMessage> of()
    {
        return new CasualServiceCallReplyMessageDecoder();
    }

    /**
     * Number of maximum bytes before any chunk reading takes place
     * Defaults to Integer.MAX_VALUE
     * @return
     */
    public static int getMaxPayloadSingleBufferByteSize()
    {
        return maxPayloadSingleBufferByteSize;
    }

    /**
     * If not set, defaults to Integer.MAX_VALUE
     * Can be used in testing to force chunked reading
     * by for instance setting it to 1
     * @return
     */
    public static void setMaxPayloadSingleBufferByteSize(int maxPayloadSingleBufferByteSize)
    {
        CasualServiceCallReplyMessageDecoder.maxPayloadSingleBufferByteSize = maxPayloadSingleBufferByteSize;
    }

    @Override
    public CasualServiceCallReplyMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        return createMessage(ByteUtils.readFully(channel, messageSize).array());
    }

    @Override
    public CasualServiceCallReplyMessage readChunked(final ReadableByteChannel channel)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(channel);
        final int callError = ByteUtils.readFully(channel, ServiceCallReplySizes.CALL_ERROR.getNetworkSize()).getInt();
        final long userError = ByteUtils.readFully(channel, ServiceCallReplySizes.CALL_CODE.getNetworkSize()).getLong();
        final Xid xid = XIDUtils.readXid(channel);
        final int transactionState = ByteUtils.readFully(channel, ServiceCallReplySizes.TRANSACTION_STATE.getNetworkSize()).get();
        final ServiceBuffer serviceBuffer = CasualMessageDecoderUtils.readServiceBuffer(channel, getMaxPayloadSingleBufferByteSize());
        return CasualServiceCallReplyMessage.createBuilder()
                                            .setExecution(execution)
                                            .setError(ErrorState.unmarshal(callError))
                                            .setUserSuppliedError(userError)
                                            .setXid(xid)
                                            .setTransactionState(TransactionState.unmarshal(transactionState))
                                            .setServiceBuffer(serviceBuffer)
                                            .build();
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

        Pair<Integer, Xid> xidInfo = CasualMessageDecoderUtils.readXid(data, currentOffset);
        currentOffset = xidInfo.first();
        final Xid xid = xidInfo.second();

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
        return CasualServiceCallReplyMessage.createBuilder()
                                            .setExecution(execution)
                                            .setError(ErrorState.unmarshal(callError))
                                            .setUserSuppliedError(userError)
                                            .setXid(xid)
                                            .setTransactionState(TransactionState.unmarshal(transactionState))
                                            .setServiceBuffer(serviceBuffer)
                                            .build();
    }
}
