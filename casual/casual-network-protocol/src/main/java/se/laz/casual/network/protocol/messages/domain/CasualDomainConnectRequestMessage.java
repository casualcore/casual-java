/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.domain;

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;
import se.laz.casual.network.protocol.messages.parseinfo.ConnectRequestSizes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public final class CasualDomainConnectRequestMessage implements CasualNetworkTransmittable
{
    private UUID execution;
    private UUID domainId;
    private String domainName;
    private List<Long> protocols;

    private CasualDomainConnectRequestMessage(final UUID execution, final UUID domainId, final String domainName, final List<Long> protocols)
    {
        this.execution = execution;
        this.domainId = domainId;
        this.domainName = domainName;
        this.protocols = protocols;
    }

    @Override
    public CasualNWMessageType getType()
    {
        return CasualNWMessageType.DOMAIN_CONNECT_REQUEST;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        final byte[] domainNameBytes = domainName.getBytes(StandardCharsets.UTF_8);
        int messageSize = ConnectRequestSizes.EXECUTION.getNetworkSize() + ConnectRequestSizes.DOMAIN_ID.getNetworkSize() +
                          ConnectRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize() + domainNameBytes.length +
                          ConnectRequestSizes.PROTOCOL_VERSION_SIZE.getNetworkSize() + ConnectRequestSizes.PROTOCOL_ELEMENT_SIZE.getNetworkSize() * protocols.size();
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualEncoderUtils.writeUUID(execution, b);
        CasualEncoderUtils.writeUUID(domainId, b);
        b.putLong(domainNameBytes.length)
         .put(domainNameBytes);
        b.putLong(protocols.size());
        for(Long v : protocols)
        {
            b.putLong(v);
        }
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
        CasualDomainConnectRequestMessage that = (CasualDomainConnectRequestMessage) o;
        return Objects.equals(execution, that.execution) &&
            Objects.equals(domainId, that.domainId) &&
            Objects.equals(domainName, that.domainName) &&
            Objects.equals(protocols, that.protocols);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, domainId, domainName, protocols);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("CasualDomainConnectRequestMessage{");
        sb.append("execution=").append(execution);
        sb.append(", domainId=").append(domainId);
        sb.append(", domainName='").append(domainName).append('\'');
        sb.append(", protocols= ");
        protocols.stream()
                 .forEach(p -> sb.append(" '" + p + "' "));
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

    public List<Long> getProtocols()
    {
        return protocols.stream().collect(Collectors.toList());
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
        private List<Long> protocols;

        private Builder()
        {
        }

        public Builder withExecution(UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public Builder withDomainId(UUID domainId)
        {
            this.domainId = domainId;
            return this;
        }

        public Builder withDomainName(String domainName)
        {
            this.domainName = domainName;
            return this;
        }

        public Builder withProtocols(List<Long> protocols)
        {
            this.protocols = protocols;
            return this;
        }

        public CasualDomainConnectRequestMessage build()
        {
            Objects.requireNonNull(execution, "execution is not allowed to be null");
            Objects.requireNonNull(domainId, "domainId is not allowed to be null");
            Objects.requireNonNull(domainName, "domainName is not allowed to be null");
            Objects.requireNonNull(protocols, "protocols is not allowed to be null");
            if(protocols.isEmpty())
            {
                throw new CasualProtocolException("zero size protocol list, this makes no sense!");
            }
            return new CasualDomainConnectRequestMessage(execution, domainId, domainName, protocols);
        }
    }
}
