package se.kodarkatten.casual.network.io.readers;

import se.kodarkatten.casual.api.flags.ErrorState;
import se.kodarkatten.casual.api.flags.TransactionState;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.api.xa.XIDFormatType;
import se.kodarkatten.casual.network.io.readers.utils.CasualNetworkReaderUtils;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.messages.parseinfo.ServiceCallReplySizes;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallReplyMessage;
import se.kodarkatten.casual.network.messages.service.ServiceBuffer;
import se.kodarkatten.casual.network.utils.ByteUtils;
import se.kodarkatten.casual.network.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by aleph on 2017-03-28.
 */
public final class CasualServiceCallReplyMessageReader implements NetworkReader<CasualServiceCallReplyMessage>
{
    private static int maxPayloadSingleBufferByteSize = Integer.MAX_VALUE;
    private CasualServiceCallReplyMessageReader()
    {}

    public static NetworkReader<CasualServiceCallReplyMessage> of()
    {
        return new CasualServiceCallReplyMessageReader();
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
        CasualServiceCallReplyMessageReader.maxPayloadSingleBufferByteSize = maxPayloadSingleBufferByteSize;
    }

    @Override
    public CasualServiceCallReplyMessage readSingleBuffer(AsynchronousByteChannel channel, int messageSize)
    {
        final CompletableFuture<ByteBuffer> msgFuture = ByteUtils.readFully(channel, messageSize);
        try
        {
            return createMessage(msgFuture.get().array());
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading CasualServiceCallRequestMessage", e);
        }
    }

    @Override
    public CasualServiceCallReplyMessage readChunked(AsynchronousByteChannel channel)
    {
        try
        {
            final UUID execution = CasualNetworkReaderUtils.readUUID(channel);
            final int callDescriptor = (int) ByteUtils.readFully(channel, ServiceCallReplySizes.CALL_DESCRIPTOR.getNetworkSize()).get().getLong();
            final int callError = (int) ByteUtils.readFully(channel, ServiceCallReplySizes.CALL_ERROR.getNetworkSize()).get().getLong();
            final long userError = ByteUtils.readFully(channel, ServiceCallReplySizes.CALL_CODE.getNetworkSize()).get().getLong();
            final Xid xid = XIDUtils.readXid(channel);
            final int transactionState = (int) ByteUtils.readFully(channel, ServiceCallReplySizes.TRANSACTION_STATE.getNetworkSize()).get().getLong();
            final ServiceBuffer serviceBuffer = CasualNetworkReaderUtils.readServiceBuffer(channel, getMaxPayloadSingleBufferByteSize());
            return CasualServiceCallReplyMessage.createBuilder()
                                                .setExecution(execution)
                                                .setCallDescriptor(callDescriptor)
                                                .setError(ErrorState.unmarshal(callError))
                                                .setUserSuppliedError(userError)
                                                .setXid(xid)
                                                .setTransactionState(TransactionState.unmarshal(transactionState))
                                                .setServiceBuffer(serviceBuffer)
                                                .build();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading CasualServiceCallRequestMessage", e);
        }
    }

    private CasualServiceCallReplyMessage createMessage(final byte[] data)
    {
        int currentOffset = 0;
        final UUID execution = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(data, currentOffset, ServiceCallReplySizes.EXECUTION.getNetworkSize()));
        currentOffset += ServiceCallReplySizes.EXECUTION.getNetworkSize();

        final ByteBuffer callDescriptorBuffer = ByteBuffer.wrap(data, currentOffset, ServiceCallReplySizes.CALL_DESCRIPTOR.getNetworkSize());
        int callDescriptor = (int)callDescriptorBuffer.getLong();
        currentOffset += ServiceCallReplySizes.CALL_DESCRIPTOR.getNetworkSize();

        final ByteBuffer callErrorBuffer = ByteBuffer.wrap(data, currentOffset, ServiceCallReplySizes.CALL_ERROR.getNetworkSize());
        int callError = (int)callErrorBuffer.getLong();
        currentOffset += ServiceCallReplySizes.CALL_ERROR.getNetworkSize();

        final ByteBuffer userErrorBuffer = ByteBuffer.wrap(data, currentOffset, ServiceCallReplySizes.CALL_CODE.getNetworkSize());
        long userError = userErrorBuffer.getLong();
        currentOffset += ServiceCallReplySizes.CALL_CODE.getNetworkSize();

        long xidFormat = ByteBuffer.wrap(data, currentOffset, ServiceCallReplySizes.TRANSACTION_TRID_XID_FORMAT.getNetworkSize()).getLong();
        currentOffset += ServiceCallReplySizes.TRANSACTION_TRID_XID_FORMAT.getNetworkSize();
        Xid xid = XID.of();
        if(!XIDFormatType.isNullType(xidFormat))
        {
            int gtridLength = (int)ByteBuffer.wrap(data, currentOffset, ServiceCallReplySizes.TRANSACTION_TRID_XID_GTRID_LENGTH.getNetworkSize()).getLong();
            currentOffset += ServiceCallReplySizes.TRANSACTION_TRID_XID_GTRID_LENGTH.getNetworkSize();
            int bqualLength = (int)ByteBuffer.wrap(data, currentOffset, ServiceCallReplySizes.TRANSACTION_TRID_XID_BQUAL_LENGTH.getNetworkSize()).getLong();
            currentOffset += ServiceCallReplySizes.TRANSACTION_TRID_XID_BQUAL_LENGTH.getNetworkSize();
            ByteBuffer xidPayloadBuffer = ByteBuffer.wrap(data, currentOffset, gtridLength + bqualLength);
            final byte[] xidPayload = new byte[gtridLength + bqualLength];
            xidPayloadBuffer.get(xidPayload);
            currentOffset += (gtridLength + bqualLength);
            xid = XID.of(gtridLength, bqualLength, xidPayload, xidFormat);
        }

        final ByteBuffer transactionStateBuffer = ByteBuffer.wrap(data, currentOffset, ServiceCallReplySizes.TRANSACTION_STATE.getNetworkSize());
        int transactionState = (int)transactionStateBuffer.getLong();
        currentOffset += ServiceCallReplySizes.TRANSACTION_STATE.getNetworkSize();

        int serviceBufferTypeSize = (int) ByteBuffer.wrap(data, currentOffset, ServiceCallReplySizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ServiceCallReplySizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize();
        final String serviceTypeName = CasualNetworkReaderUtils.getAsString(data, currentOffset, serviceBufferTypeSize);
        currentOffset += serviceBufferTypeSize;
        // this can be huge, ie not fitting into one ByteBuffer
        // but since the whole message fits into Integer.MAX_VALUE that is not true of this message
        int serviceBufferPayloadSize = (int) ByteBuffer.wrap(data, currentOffset, ServiceCallReplySizes.BUFFER_PAYLOAD_SIZE.getNetworkSize()).getLong();
        currentOffset += ServiceCallReplySizes.BUFFER_PAYLOAD_SIZE.getNetworkSize();
        final ByteBuffer serviceBufferPayloadBuffer =  ByteBuffer.wrap(data, currentOffset, serviceBufferPayloadSize);
        final List<byte[]> serviceBufferPayload = new ArrayList<>();
        serviceBufferPayload.add(serviceBufferPayloadBuffer.array());
        final ServiceBuffer serviceBuffer = ServiceBuffer.of(serviceTypeName, serviceBufferPayload);
        return CasualServiceCallReplyMessage.createBuilder()
                                            .setExecution(execution)
                                            .setCallDescriptor(callDescriptor)
                                            .setError(ErrorState.unmarshal(callError))
                                            .setUserSuppliedError(userError)
                                            .setXid(xid)
                                            .setTransactionState(TransactionState.unmarshal(transactionState))
                                            .setServiceBuffer(serviceBuffer)
                                            .build();
    }
}

