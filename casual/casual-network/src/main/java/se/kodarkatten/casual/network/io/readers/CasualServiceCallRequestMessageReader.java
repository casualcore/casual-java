package se.kodarkatten.casual.network.io.readers;

import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.api.xa.XIDFormatType;
import se.kodarkatten.casual.network.io.readers.utils.CasualNetworkReaderUtils;
import se.kodarkatten.casual.network.messages.common.ServiceBuffer;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.messages.parseinfo.ServiceCallRequestSizes;
import se.kodarkatten.casual.network.messages.request.service.CasualServiceCallRequestMessage;
import se.kodarkatten.casual.network.utils.ByteUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by aleph on 2017-03-16.
 */
public final class CasualServiceCallRequestMessageReader
{
    private static int maxSingleBufferByteSize = Integer.MAX_VALUE;
    private CasualServiceCallRequestMessageReader()
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
        CasualServiceCallRequestMessageReader.maxSingleBufferByteSize = maxSingleBufferByteSize;
    }

    /**
     * It is upon the caller to close the channel
     * @param channel
     * @param messageSize
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    // For now, it is the caller responsibility to close the channel
    @SuppressWarnings("squid:S2095")
    public static CasualServiceCallRequestMessage read(final AsynchronousByteChannel channel, long messageSize)
    {
        Objects.requireNonNull(channel, "channel is null");
        try
        {
            if (messageSize <= getMaxSingleBufferByteSize())
            {
                return readSingleBuffer(channel, (int) messageSize);
            }
            return readChunked(channel);
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading", e);
        }
    }

    private static CasualServiceCallRequestMessage readSingleBuffer(final AsynchronousByteChannel channel, int messageSize) throws ExecutionException, InterruptedException
    {
        final CompletableFuture<ByteBuffer> msgFuture = ByteUtils.readFully(channel, messageSize);
        return readMessage(msgFuture.get().array());
    }

    private static CasualServiceCallRequestMessage readMessage(final byte[] data)
    {
        int currentOffset = 0;
        final UUID execution = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(data, currentOffset, ServiceCallRequestSizes.EXECUTION.getNetworkSize()));
        currentOffset += ServiceCallRequestSizes.EXECUTION.getNetworkSize();

        final ByteBuffer callDescriptorBuffer = ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.CALL_DESCRIPTOR.getNetworkSize());
        int callDescriptor = (int)callDescriptorBuffer.getLong();
        currentOffset += ServiceCallRequestSizes.CALL_DESCRIPTOR.getNetworkSize();

        int serviceNameLen = (int)ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.SERVICE_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.SERVICE_NAME_SIZE.getNetworkSize();
        final String serviceName = CasualNetworkReaderUtils.getAsString(data, currentOffset, (int)serviceNameLen);
        currentOffset += serviceNameLen;

        long timeout  = ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.SERVICE_TIMEOUT.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.SERVICE_TIMEOUT.getNetworkSize();

        final int parentNameSize = (int)ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.PARENT_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.PARENT_NAME_SIZE.getNetworkSize();
        final String parentName = CasualNetworkReaderUtils.getAsString(data, currentOffset, parentNameSize);
        currentOffset += parentNameSize;

        int xidFormat = (int)ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.XID_FORMAT.getNetworkSize()).getLong();
        XIDFormatType xidFormatType = XIDFormatType.unmarshal(xidFormat);
        currentOffset += ServiceCallRequestSizes.XID_FORMAT.getNetworkSize();
        XID xid = XID.of();
        if(XIDFormatType.NULL != xidFormatType)
        {
            int gtridLength = (int)ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.XID_GTRID_LENGTH.getNetworkSize()).getLong();
            currentOffset += ServiceCallRequestSizes.XID_GTRID_LENGTH.getNetworkSize();
            int bqualLength = (int)ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.XID_BQUAL_LENGTH.getNetworkSize()).getLong();
            currentOffset += ServiceCallRequestSizes.XID_BQUAL_LENGTH.getNetworkSize();
            ByteBuffer xidPayload = ByteBuffer.wrap(data, currentOffset, gtridLength + bqualLength);
            currentOffset += (gtridLength + bqualLength);
            xid = XID.of(gtridLength, bqualLength, xidPayload.array(), xidFormatType);
        }
        int flags = (int) ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.FLAGS.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.FLAGS.getNetworkSize();
        int serviceBufferTypeSize = (int) ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize();
        final String serviceTypeName = CasualNetworkReaderUtils.getAsString(data, currentOffset, serviceBufferTypeSize);
        currentOffset += serviceBufferTypeSize;
        // this can be huge, ie not fitting into one ByteBuffer
        // but since the whole message fits into Integer.MAX_VALUE that is not true of this message
        int serviceBufferPayloadSize = (int) ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize();
        final ByteBuffer serviceBufferPayloadBuffer =  ByteBuffer.wrap(data, currentOffset, serviceBufferPayloadSize);
        final List<byte[]> serviceBufferPayload = new ArrayList<>();
        serviceBufferPayload.add(serviceBufferPayloadBuffer.array());
        final ServiceBuffer serviceBuffer = ServiceBuffer.of(serviceTypeName, serviceBufferPayload);
        return CasualServiceCallRequestMessage.createBuilder()
                                              .setExecution(execution)
                                              .setCallDescriptor(callDescriptor)
                                              .setServiceName(serviceName)
                                              .setTimeout(timeout)
                                              .setParentName(parentName)
                                              .setXid(xid)
                                              .setXatmiFlags(new Flag.Builder(flags).build())
                                              .setServiceBuffer(serviceBuffer)
                                              .build();
    }

    private static CasualServiceCallRequestMessage readChunked(AsynchronousByteChannel channel)
    {
        throw new NotImplementedException();
    }

}
