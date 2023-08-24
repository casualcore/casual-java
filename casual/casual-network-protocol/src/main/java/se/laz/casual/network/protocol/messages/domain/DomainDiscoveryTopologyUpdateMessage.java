/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.domain;

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.DiscoveryTopologyUpdateRequestSizes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DomainDiscoveryTopologyUpdateMessage implements CasualNetworkTransmittable
{
    private UUID execution;
    private long domainsSize;
    private UUID domainId;
    private String domainName;

    private DomainDiscoveryTopologyUpdateMessage(Builder builder)
    {
        execution = builder.execution;
        domainsSize = builder.domainsSize;
        domainId = builder.domainId;
        domainName = builder.domainName;
    }

    public UUID getExecution()
    {
        return execution;
    }

    public long getDomainsSize()
    {
        return domainsSize;
    }

    public UUID getDomainId()
    {
        return domainId;
    }

    public String getDomainName()
    {
        return domainName;
    }

    @Override
    public CasualNWMessageType getType()
    {
        return CasualNWMessageType.DOMAIN_DISCOVERY_TOPOLOGY_UPDATE;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        final byte[] domainNameBytes = domainName.getBytes(StandardCharsets.UTF_8);
        int messageSize = DiscoveryTopologyUpdateRequestSizes.EXECUTION.getNetworkSize() + DiscoveryTopologyUpdateRequestSizes.DOMAIN_ID.getNetworkSize() +
                DiscoveryTopologyUpdateRequestSizes.DOMAINS_SIZE.getNetworkSize() +
                DiscoveryTopologyUpdateRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize() + domainNameBytes.length;
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualEncoderUtils.writeUUID(execution, b);
        b.putLong(domainsSize);
        CasualEncoderUtils.writeUUID(domainId, b);
        b.putLong(domainNameBytes.length)
         .put(domainNameBytes);
        List<byte[]> l = new ArrayList<>();
        l.add(b.array());
        return l;
    }

    @Override
    public List<ProtocolVersion> supportedProtocolVersions()
    {
        return Arrays.asList(ProtocolVersion.VERSION_1_2);
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
        DomainDiscoveryTopologyUpdateMessage that = (DomainDiscoveryTopologyUpdateMessage) o;
        return domainsSize == that.domainsSize && Objects.equals(execution, that.execution) && Objects.equals(domainId, that.domainId) && Objects.equals(domainName, that.domainName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, domainsSize, domainId, domainName);
    }

    @Override
    public String toString()
    {
        return "DomainDiscoveryTopologyUpdateMessage{" +
                "execution=" + execution +
                ", domainsSize=" + domainsSize +
                ", domainId=" + domainId +
                ", domainName='" + domainName + '\'' +
                '}';
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private UUID execution;
        private long domainsSize;
        private UUID domainId;
        private String domainName;

        public Builder()
        {}

        public Builder withExecution(UUID execution)
        {
            Objects.requireNonNull(execution, "execution can not be null");
            this.execution = execution;
            return this;
        }

        public Builder withDomainsSize(long domainsSize)
        {
            this.domainsSize = domainsSize;
            return this;
        }

        public Builder withDomainId(UUID domainId)
        {
            Objects.requireNonNull(domainId, "domainId can not be null");
            this.domainId = domainId;
            return this;
        }

        public Builder withDomainName(String domainName)
        {
            Objects.requireNonNull(domainName, "domainName can not be null");
            this.domainName = domainName;
            return this;
        }

        public DomainDiscoveryTopologyUpdateMessage build()
        {
            Objects.requireNonNull(execution, "execution can not be null");
            Objects.requireNonNull(domainId, "domainId can not be null");
            Objects.requireNonNull(domainName, "domainName can not be null");
            return new DomainDiscoveryTopologyUpdateMessage(this);
        }
    }
}
