package se.kodarkatten.casual.network.messages.request.service;

import se.kodarkatten.casual.api.flags.AtmiFlags;
import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.api.xa.XIDFormatType;
import se.kodarkatten.casual.network.io.writers.utils.CasualNetworkWriterUtils;
import se.kodarkatten.casual.network.messages.CasualNWMessageType;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.network.messages.common.ServiceBuffer;
import se.kodarkatten.casual.network.messages.parseinfo.ServiceCallRequestSizes;
import se.kodarkatten.casual.network.utils.ByteUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-14.
 */
// lambdas are fine
@SuppressWarnings("squid:S1612")
public final class CasualServiceCallRequestMessage implements CasualNetworkTransmittable
{
    private UUID execution;
    private int callDescriptor;
    private String serviceName;
    private long timeout;
    private String parentName;
    private Xid xid;
    private Flag<AtmiFlags> xatmiFlags;
    private ServiceBuffer serviceBuffer;

    // not part of the message
    // used for testing
    // so that we can get chunks without having to have a huge message
    // Defaults to Integer.MAX_VALUE
    private int maxMessageSize = Integer.MAX_VALUE;

    private CasualServiceCallRequestMessage()
    {}

    @Override
    public CasualNWMessageType getType()
    {
        return CasualNWMessageType.SERVICE_CALL_REQUEST;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        final byte[] serviceNameBytes = serviceName.getBytes(StandardCharsets.UTF_8);
        final byte[] parentNameBytes = parentName.getBytes(StandardCharsets.UTF_8);
        final List<byte[]> serviceBytes = serviceBuffer.toNetworkBytes();
        final long messageSize = ServiceCallRequestSizes.EXECUTION.getNetworkSize() +
                                 ServiceCallRequestSizes.CALL_DESCRIPTOR.getNetworkSize() +
                                 ServiceCallRequestSizes.SERVICE_NAME_SIZE.getNetworkSize() + serviceNameBytes.length +
                                 ServiceCallRequestSizes.SERVICE_TIMEOUT.getNetworkSize() +
                                 ServiceCallRequestSizes.PARENT_NAME_SIZE.getNetworkSize() + parentNameBytes.length +
                                 getXIDNetworkSize() +
                                 ServiceCallRequestSizes.FLAGS.getNetworkSize() +
                                 ServiceCallRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize() + + ServiceCallRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize() + ByteUtils.sumNumberOfBytes(serviceBytes);
        return (messageSize <= getMaxMessageSize()) ? toNetworkBytesFitsInOneBuffer((int)messageSize, serviceNameBytes, parentNameBytes, serviceBytes)
                                                    : toNetworkBytesMultipleBuffers(serviceNameBytes, parentNameBytes, serviceBytes);
    }



    private List<byte[]> toNetworkBytesFitsInOneBuffer(int messageSize, final byte[] serviceNameBytes, final byte[] parentNameBytes, final List<byte[]> serviceBytes)
    {
        List<byte[]> l = new ArrayList<>();
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualNetworkWriterUtils.writeUUID(execution, b);
        b.putLong(callDescriptor)
         .putLong(serviceNameBytes.length)
         .put(serviceNameBytes)
         .putLong(timeout)
         .putLong(parentNameBytes.length)
         .put(parentNameBytes);
        writeXID(xid, b);
        b.putLong(xatmiFlags.getFlagValue())
         .putLong(serviceBytes.get(0).length)
         .put(serviceBytes.get(0));
        serviceBytes.remove(0);
        final long payloadSize = ByteUtils.sumNumberOfBytes(serviceBytes);
        b.putLong(payloadSize);
        serviceBytes.stream()
                    .forEach(bytes -> b.put(bytes));
        l.add(b.array());
        return l;
    }

    private List<byte[]> toNetworkBytesMultipleBuffers(final byte[] serviceNameBytes, final byte[] parentNameBytes, final List<byte[]> serviceBytes)
    {
        final List<byte[]> l = new ArrayList<>();
        final ByteBuffer executionBuffer = ByteBuffer.allocate(ServiceCallRequestSizes.EXECUTION.getNetworkSize());
        CasualNetworkWriterUtils.writeUUID(execution, executionBuffer);
        l.add(executionBuffer.array());
        final ByteBuffer callDescriptorBuffer = ByteBuffer.allocate(ServiceCallRequestSizes.CALL_DESCRIPTOR.getNetworkSize());
        callDescriptorBuffer.putLong(callDescriptor);
        l.add(callDescriptorBuffer.array());
        final ByteBuffer serviceNameSizeBuffer = ByteBuffer.allocate(ServiceCallRequestSizes.SERVICE_NAME_SIZE.getNetworkSize());
        serviceNameSizeBuffer.putLong(serviceNameBytes.length);
        l.add(serviceNameSizeBuffer.array());
        l.add(serviceNameBytes);
        final ByteBuffer timeoutBuffer = ByteBuffer.allocate(ServiceCallRequestSizes.SERVICE_TIMEOUT.getNetworkSize());
        timeoutBuffer.putLong(timeout);
        l.add(timeoutBuffer.array());
        final ByteBuffer parentNameSizeBuffer = ByteBuffer.allocate(ServiceCallRequestSizes.PARENT_NAME_SIZE.getNetworkSize());
        parentNameSizeBuffer.putLong(parentNameBytes.length);
        l.add(parentNameSizeBuffer.array());
        l.add(parentNameBytes);
        final ByteBuffer xidByteBuffer = ByteBuffer.allocate(getXIDNetworkSize());
        writeXID(xid, xidByteBuffer);
        l.add(xidByteBuffer.array());
        final ByteBuffer flagBuffer = ByteBuffer.allocate(ServiceCallRequestSizes.FLAGS.getNetworkSize());
        flagBuffer.putLong(xatmiFlags.getFlagValue());
        l.add(flagBuffer.array());
        final ByteBuffer serviceBufferTypeSize = ByteBuffer.allocate(ServiceCallRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize());
        serviceBufferTypeSize.putLong(serviceBytes.get(0).length);
        l.add(serviceBufferTypeSize.array());
        l.add(serviceBytes.get(0));
        serviceBytes.remove(0);
        final long payloadSize = ByteUtils.sumNumberOfBytes(serviceBytes);
        final ByteBuffer serviceBufferPayloadSizeBuffer = ByteBuffer.allocate(ServiceCallRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize());
        serviceBufferPayloadSizeBuffer.putLong(payloadSize);
        l.add(serviceBufferPayloadSizeBuffer.array());
        serviceBytes.stream()
                    .forEach(bytes -> l.add(bytes));
        return l;
    }

    private ByteBuffer writeXID(final Xid xid, final ByteBuffer b)
    {
        b.putLong(xid.getFormatId());
        if(!XIDFormatType.isNullType(xid.getFormatId()))
        {
            final byte[] gtridId = xid.getGlobalTransactionId();
            final byte[] bqual = xid.getBranchQualifier();
            b.putLong(gtridId.length)
             .putLong(bqual.length)
             .put(gtridId)
             .put(bqual);
        }
        return b;
    }

    private int getXIDNetworkSize()
    {
        if(XIDFormatType.isNullType(xid.getFormatId()))
        {
            return ServiceCallRequestSizes.XID_FORMAT.getNetworkSize();
        }
        final byte[] gtridId = xid.getGlobalTransactionId();
        final byte[] bqual = xid.getBranchQualifier();
        return ServiceCallRequestSizes.XID_FORMAT.getNetworkSize() +
               ServiceCallRequestSizes.XID_GTRID_LENGTH.getNetworkSize() +
               ServiceCallRequestSizes.XID_BQUAL_LENGTH.getNetworkSize() +
               gtridId.length + bqual.length;
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public UUID getExecution()
    {
        return execution;
    }

    public int getCallDescriptor()
    {
        return callDescriptor;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public String getParentName()
    {
        return parentName;
    }

    public Xid getXid()
    {
        return XID.of(xid);
    }

    public Flag<AtmiFlags> getXatmiFlags()
    {
        return Flag.of(xatmiFlags);
    }

    /**
     * Defaults to Integer.MAX_VALUE
     * @return
     */
    public int getMaxMessageSize()
    {
        return maxMessageSize;
    }



    /**
     * Use in testing to force chunking of message
     * @param maxMessageSize
     * @return
     */
    public CasualServiceCallRequestMessage setMaxMessageSize(int maxMessageSize)
    {
        this.maxMessageSize = maxMessageSize;
        return this;
    }

    /**
     * Note, not immutable
     * @return
     */
    public ServiceBuffer getServiceBuffer()
    {
        return serviceBuffer;
    }

    @SuppressWarnings("squid:S1067")
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        CasualServiceCallRequestMessage that = (CasualServiceCallRequestMessage) o;
        return callDescriptor == that.callDescriptor &&
            Objects.equals(execution, that.execution) &&
            Objects.equals(serviceName, that.serviceName) &&
            Objects.equals(parentName, that.parentName) &&
            Objects.equals(xid, that.xid) &&
            Objects.equals(xatmiFlags, that.xatmiFlags) &&
            Objects.equals(serviceBuffer.getType(), that.serviceBuffer.getType());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, callDescriptor, serviceName, parentName, xid, xatmiFlags);
    }

    public static class Builder
    {
        private UUID execution;
        private int callDescriptor;
        private String serviceName;
        private long timeout;
        private String parentName;
        private Xid xid;
        private Flag<AtmiFlags> xatmiFlags;
        private ServiceBuffer serviceBuffer;

        public Builder setExecution(UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public Builder setCallDescriptor(int callDescriptor)
        {
            this.callDescriptor = callDescriptor;
            return this;
        }

        public Builder setServiceName(String serviceName)
        {
            this.serviceName = serviceName;
            return this;
        }

        public Builder setTimeout(long timeout)
        {
            this.timeout = timeout;
            return this;
        }

        public Builder setParentName(String parentName)
        {
            this.parentName = parentName;
            return this;
        }

        public Builder setXid(Xid xid)
        {
            this.xid = xid;
            return this;
        }

        public Builder setXatmiFlags(Flag<AtmiFlags> xatmiFlags)
        {
            this.xatmiFlags = xatmiFlags;
            return this;
        }

        public Builder setServiceBuffer(ServiceBuffer serviceBuffer)
        {
            this.serviceBuffer = serviceBuffer;
            return this;
        }
        public CasualServiceCallRequestMessage build()
        {
            CasualServiceCallRequestMessage r = new CasualServiceCallRequestMessage();
            r.execution = execution;
            r.callDescriptor = callDescriptor;
            r.serviceName = serviceName;
            r.timeout = timeout;
            r.parentName = parentName;
            r.xid = XID.of(xid);
            r.xatmiFlags = xatmiFlags;
            r.serviceBuffer = serviceBuffer;
            return r;
        }
    }

}
