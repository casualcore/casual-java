/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.network.protocol.messages;

import java.util.List;

/**
 * Created by aleph on 2017-03-09.
 */
public interface CasualNetworkTransmittable
{
    CasualNWMessageType getType();
    List<byte[]> toNetworkBytes();
}
