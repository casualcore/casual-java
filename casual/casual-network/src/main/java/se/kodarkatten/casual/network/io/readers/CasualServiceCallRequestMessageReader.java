package se.kodarkatten.casual.network.io.readers;

import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.api.xa.XIDFormatType;
import se.kodarkatten.casual.network.io.readers.utils.CasualNetworkReaderUtils;
import se.kodarkatten.casual.network.messages.service.ServiceBuffer;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.messages.parseinfo.ServiceCallRequestSizes;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage;
import se.kodarkatten.casual.network.utils.ByteUtils;
import se.kodarkatten.casual.network.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by aleph on 2017-03-16.
 */
public final class CasualServiceCallRequestMessageReader implements NetworkReader<CasualServiceCallRequestMessage>
{
    private static int maxPayloadSingleBufferByteSize = Integer.MAX_VALUE;
    private CasualServiceCallRequestMessageReader()
    {}

    public static NetworkReader<CasualServiceCallRequestMessage> of()
    {
        return new CasualServiceCallRequestMessageReader();
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
        CasualServiceCallRequestMessageReader.maxPayloadSingleBufferByteSize = maxPayloadSingleBufferByteSize;
    }

    public CasualServiceCallRequestMessage readSingleBuffer(final AsynchronousByteChannel channel, int messageSize)
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

    private static CasualServiceCallRequestMessage createMessage(final byte[] data)
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

        long xidFormat = ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.XID_FORMAT.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.XID_FORMAT.getNetworkSize();
        Xid xid = XID.of();
        if(!XIDFormatType.isNullType(xidFormat))
        {
            int gtridLength = (int)ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.XID_GTRID_LENGTH.getNetworkSize()).getLong();
            currentOffset += ServiceCallRequestSizes.XID_GTRID_LENGTH.getNetworkSize();
            int bqualLength = (int)ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.XID_BQUAL_LENGTH.getNetworkSize()).getLong();
            currentOffset += ServiceCallRequestSizes.XID_BQUAL_LENGTH.getNetworkSize();
            ByteBuffer xidPayloadBuffer = ByteBuffer.wrap(data, currentOffset, gtridLength + bqualLength);
            final byte[] xidPayload = new byte[gtridLength + bqualLength];
            xidPayloadBuffer.get(xidPayload);
            currentOffset += (gtridLength + bqualLength);
            xid = XID.of(gtridLength, bqualLength, xidPayload, xidFormat);
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

    public CasualServiceCallRequestMessage readChunked(final AsynchronousByteChannel channel)
    {
        try
        {
            final UUID execution = CasualNetworkReaderUtils.readUUID(channel);
            final int callDescriptor = (int) ByteUtils.readFully(channel, ServiceCallRequestSizes.CALL_DESCRIPTOR.getNetworkSize()).get().getLong();
            final int serviceNameSize = (int) ByteUtils.readFully(channel, ServiceCallRequestSizes.SERVICE_NAME_SIZE.getNetworkSize()).get().getLong();
            final String serviceName = CasualNetworkReaderUtils.readString(channel, serviceNameSize);
            final long serviceTimeout = ByteUtils.readFully(channel, ServiceCallRequestSizes.SERVICE_TIMEOUT.getNetworkSize()).get().getLong();
            final int parentNameSize = (int) ByteUtils.readFully(channel, ServiceCallRequestSizes.PARENT_NAME_SIZE.getNetworkSize()).get().getLong();
            final String parentName = CasualNetworkReaderUtils.readString(channel, parentNameSize);
            final Xid xid = XIDUtils.readXid(channel);
            final int flags = (int) ByteUtils.readFully(channel, ServiceCallRequestSizes.FLAGS.getNetworkSize()).get().getLong();
            final ServiceBuffer buffer = CasualNetworkReaderUtils.readServiceBuffer(channel, getMaxPayloadSingleBufferByteSize());
            return CasualServiceCallRequestMessage.createBuilder()
                                                  .setExecution(execution)
                                                  .setCallDescriptor(callDescriptor)
                                                  .setServiceName(serviceName)
                                                  .setTimeout(serviceTimeout)
                                                  .setParentName(parentName)
                                                  .setXid(xid)
                                                  .setXatmiFlags(new Flag.Builder(flags).build())
                                                  .setServiceBuffer(buffer)
                                                  .build();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading CasualServiceCallRequestMessage", e);
        }
    }

}