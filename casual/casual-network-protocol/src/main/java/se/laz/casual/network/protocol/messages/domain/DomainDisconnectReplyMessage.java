/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.domain;

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.api.util.PrettyPrinter;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.DisconnectRequestSizes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DomainDisconnectReplyMessage implements CasualNetworkTransmittable
{
    private static final List<ProtocolVersion> SUPPORTED_VERSIONS = Arrays.asList(ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2);
    private final UUID execution;

    public DomainDisconnectReplyMessage(UUID execution)
    {
        this.execution = execution;
    }

    public static DomainDisconnectReplyMessage of(final UUID execution)
    {
        Objects.requireNonNull(execution, "execution can not be null");
        return new DomainDisconnectReplyMessage(execution);
    }

    @Override
    public CasualNWMessageType getType()
    {
        return CasualNWMessageType.DOMAIN_DISCONNECT_REPLY;
    }

    @Override
    public List<ProtocolVersion> supportedProtocolVersions()
    {
        return SUPPORTED_VERSIONS;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        int messageSize = DisconnectRequestSizes.EXECUTION.getNetworkSize();
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualEncoderUtils.writeUUID(execution, b);
        List<byte[]> l = new ArrayList<>();
        l.add(b.array());
        return l;
    }

    @Override
    public String toString()
    {
        return "DomainDisconnectReplyMessage{" +
                "execution=" + execution + " ( " + PrettyPrinter.casualStringify(execution) + " )" +
                '}';
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
        DomainDisconnectReplyMessage that = (DomainDisconnectReplyMessage) o;
        return Objects.equals(execution, that.execution) && getType() == that.getType();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution);
    }

}
