/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.network.protocol.messages;

import java.util.List;
import java.util.UUID;

/**
 * Interface for a casual nw message that can be sent on the wire
 * @param <T> the type
 */
public interface CasualNWMessage<T extends CasualNetworkTransmittable>
{
    CasualNWMessageType getType();
    List<byte[]> toNetworkBytes();
    UUID getCorrelationId();
    T getMessage();
}
