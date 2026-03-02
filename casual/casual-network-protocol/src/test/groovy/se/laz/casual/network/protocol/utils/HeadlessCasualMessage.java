/*
 * Copyright (c) 2025 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.utils;

import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.decoding.CasualMessageDecoder;
import se.laz.casual.network.protocol.messages.CasualNWMessageHeader;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record HeadlessCasualMessage(CasualNWMessageType type, String base64Body, ProtocolVersion protocolVersion, String template)
{
    public HeadlessCasualMessage
    {
        Objects.requireNonNull(type, "type can not be null");
        Objects.requireNonNull(base64Body, "body can not be null");
        Objects.requireNonNull(protocolVersion, "protocolVersion can not be null");
        Objects.requireNonNull(template, "template can not be null");
    }

    public String completeMessageBase64()
    {
        byte[] body = Base64.getDecoder().decode(base64Body);
        CasualNWMessageHeader header = CasualNWMessageHeader.createBuilder()
                                                            .setCorrelationId(UUID.randomUUID())
                                                            .setType(type)
                                                            .setPayloadSize(body.length)
                                                            .build();
        CasualNWMessage<?> completeMessage = CasualMessageDecoder.read(body, header, () -> protocolVersion);
        List<byte[]> messageBytes = completeMessage.toNetworkBytes();
        ByteBuffer buffer = ByteBuffer.allocate(messageBytes.stream()
                                                            .mapToInt(bytes -> bytes.length)
                                                            .sum());
        messageBytes.forEach(buffer::put);
        return Base64.getEncoder().encodeToString(buffer.array());
    }

}
