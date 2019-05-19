/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.queue;

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.messages.parseinfo.DequeueRequestSizes;
import se.laz.casual.network.protocol.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class CasualDequeueRequestMessage implements CasualNetworkTransmittable
{
    private final UUID execution;
    private final String queueName;
    private final Xid xid;
    private final String selectorProperties;
    private final UUID selectorUUID;
    private final boolean block;

    private CasualDequeueRequestMessage(final UUID execution, final String queueName, final Xid xid, final String selectorProperties, final UUID selectorUUID, boolean block)
    {
        this.execution = execution;
        this.queueName = queueName;
        this.xid = xid;
        this.selectorProperties = selectorProperties;
        this.selectorUUID = selectorUUID;
        this.block = block;
    }

    @Override
    public CasualNWMessageType getType()
    {
        return CasualNWMessageType.DEQUEUE_REQUEST;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        byte[] queueNameBytes = queueName.getBytes(StandardCharsets.UTF_8);
        byte[] selectorPropertiesBytes = selectorProperties.getBytes(StandardCharsets.UTF_8);
        int messageSize = CommonSizes.EXECUTION.getNetworkSize() +
                                 DequeueRequestSizes.NAME_SIZE.getNetworkSize() +  queueNameBytes.length +
                                 XIDUtils.getXIDNetworkSize(xid) +
                                 DequeueRequestSizes.SELECTOR_PROPERTIES_SIZE.getNetworkSize() + selectorPropertiesBytes.length +
                                 DequeueRequestSizes.SELECTOR_ID_SIZE.getNetworkSize() + DequeueRequestSizes.BLOCK.getNetworkSize();
        return toNetworkBytesFitsInOneBuffer(messageSize, queueNameBytes, selectorPropertiesBytes);
    }

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
        CasualDequeueRequestMessage that = (CasualDequeueRequestMessage) o;
        return block == that.block &&
            Objects.equals(execution, that.execution) &&
            Objects.equals(queueName, that.queueName) &&
            Objects.equals(xid, that.xid) &&
            Objects.equals(selectorProperties, that.selectorProperties) &&
            Objects.equals(selectorUUID, that.selectorUUID);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, queueName, xid, selectorProperties, selectorUUID, block);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("CasualDequeueRequestMessage{");
        sb.append("execution=").append(execution);
        sb.append(", queueName='").append(queueName).append('\'');
        sb.append(", xid=").append(xid);
        sb.append(", selectorProperties='").append(selectorProperties).append('\'');
        sb.append(", selectorUUID=").append(selectorUUID);
        sb.append(", block=").append(block);
        sb.append('}');
        return sb.toString();
    }

    public UUID getExecution()
    {
        return execution;
    }

    public String getQueueName()
    {
        return queueName;
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public Xid getXid()
    {
        return xid;
    }

    /**
     * note, may be the empty string
     *
     * @return
     */
    public String getSelectorProperties()
    {
        return selectorProperties;
    }

    /**
     * note, may be the empty UUID - all 0's
     *
     * @return
     */
    public UUID getSelectorUUID()
    {
        return selectorUUID;
    }

    public boolean isBlock()
    {
        return block;
    }

    public static final class Builder
    {
        private UUID execution;
        private String queueName;
        private Xid xid;
        private String selectorProperties;
        private UUID selectorUUID;
        private Boolean block;

        private Builder()
        {
        }

        public Builder withBlock(boolean block)
        {
            this.block = block;
            return this;
        }

        public Builder withExecution(final UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public Builder withSelectorProperties(final String selectorProperties)
        {
            this.selectorProperties = selectorProperties;
            return this;
        }

        public Builder withQueueName(final String queueName)
        {
            this.queueName = queueName;
            return this;
        }

        public Builder withXid(final Xid xid)
        {
            this.xid = xid;
            return this;
        }


        public Builder withSelectorUUID(final UUID selectorUUID)
        {
            this.selectorUUID = selectorUUID;
            return this;
        }

        public CasualDequeueRequestMessage build()
        {
            Objects.requireNonNull(execution, "execution is not allowed to be null");
            Objects.requireNonNull(queueName, "queuename is not allowed to be null");
            Objects.requireNonNull(xid, "xid is not allowed to be null");
            Objects.requireNonNull(selectorProperties, "selectorProperties is null, this is not allowed. It can be empty but not null");
            Objects.requireNonNull(selectorUUID, "selectorUUID is null, this is not allowed. It can be empty but not null ( empty being a uuid with only 0's)");
            Objects.requireNonNull(block, "block is null, this is not allowed");
            return new CasualDequeueRequestMessage(execution, queueName, xid, selectorProperties, selectorUUID, block);
        }
    }

    private List<byte[]> toNetworkBytesFitsInOneBuffer(int messageSize, final byte[] queueNameBytes, final byte[] selectorPropertiesBytes)
    {
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualEncoderUtils.writeUUID(execution, b);
        b.putLong(queueNameBytes.length)
         .put(queueNameBytes);
        CasualEncoderUtils.writeXID(xid, b);
        b.putLong(selectorPropertiesBytes.length)
         .put(selectorPropertiesBytes);
        CasualEncoderUtils.writeUUID(selectorUUID, b);
        b.put((byte) (block ? 1 : 0));
        List<byte[]> l = new ArrayList<>();
        l.add(b.array());
        return l;
    }

}
