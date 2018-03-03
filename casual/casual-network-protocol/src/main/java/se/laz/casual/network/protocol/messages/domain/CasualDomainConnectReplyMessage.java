/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.domain;

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.ConnectReplySizes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class CasualDomainConnectReplyMessage implements CasualNetworkTransmittable
{
    private UUID execution;
    private UUID domainId;
    private String domainName;
    private long protocolVersion;

    private CasualDomainConnectReplyMessage(final UUID execution, final UUID domainId, final String domainName, long protocolVersion)
    {
        this.execution = execution;
        this.domainId = domainId;
        this.domainName = domainName;
        this.protocolVersion = protocolVersion;
    }

    @Override
    public CasualNWMessageType getType()
    {
        return CasualNWMessageType.DOMAIN_CONNECT_REPLY;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        final byte[] domainNameBytes = domainName.getBytes(StandardCharsets.UTF_8);
        int messageSize = ConnectReplySizes.EXECUTION.getNetworkSize() + ConnectReplySizes.DOMAIN_ID.getNetworkSize() +
            ConnectReplySizes.DOMAIN_NAME_SIZE.getNetworkSize() + domainNameBytes.length +
            ConnectReplySizes.PROTOCOL_VERSION_SIZE.getNetworkSize();
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualEncoderUtils.writeUUID(execution, b);
        CasualEncoderUtils.writeUUID(domainId, b);
        b.putLong(domainNameBytes.length)
         .put(domainNameBytes);
        b.putLong(protocolVersion);
        List<byte[]> l = new ArrayList<>();
        l.add(b.array());
        return l;
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
        CasualDomainConnectReplyMessage that = (CasualDomainConnectReplyMessage) o;
        return protocolVersion == that.protocolVersion &&
            Objects.equals(execution, that.execution) &&
            Objects.equals(domainId, that.domainId) &&
            Objects.equals(domainName, that.domainName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, domainId, domainName, protocolVersion);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("CasualDomainConnectReplyMessage{");
        sb.append("execution=").append(execution);
        sb.append(", domainId=").append(domainId);
        sb.append(", domainName='").append(domainName).append('\'');
        sb.append(", protocolVersion=").append(protocolVersion);
        sb.append('}');
        return sb.toString();
    }

    public UUID getExecution()
    {
        return execution;
    }

    public UUID getDomainId()
    {
        return domainId;
    }

    public String getDomainName()
    {
        return domainName;
    }

    public long getProtocolVersion()
    {
        return protocolVersion;
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private UUID execution;
        private UUID domainId;
        private String domainName;
        private long protocolVersion;

        private Builder()
        {
        }

        public Builder withDomainId(UUID domainId)
        {
            this.domainId = domainId;
            return this;
        }

        public Builder withExecution(UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public Builder withProtocolVersion(long protocolVersion)
        {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder withDomainName(String domainName)
        {
            this.domainName = domainName;
            return this;
        }

        public CasualDomainConnectReplyMessage build()
        {
            Objects.requireNonNull(execution, "execution is not allowed to be null");
            Objects.requireNonNull(domainId, "domainId is not allowed to be null");
            Objects.requireNonNull(domainName, "domainName is not allowed to be null");
            return new CasualDomainConnectReplyMessage(execution, domainId, domainName, protocolVersion);
        }
    }
}
