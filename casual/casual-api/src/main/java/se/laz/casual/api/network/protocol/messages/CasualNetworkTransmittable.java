/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.network.protocol.messages;

import se.laz.casual.network.ProtocolVersion;

import java.util.Arrays;
import java.util.List;

/**
 * Interface for message that can be sent on the wire
 * Created by aleph on 2017-03-09.
 */
public interface CasualNetworkTransmittable
{
    CasualNWMessageType getType();
    List<byte[]> toNetworkBytes();
    default List<ProtocolVersion> supportedProtocolVersions()
    {
        return Arrays.asList(ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2);
    }
}
