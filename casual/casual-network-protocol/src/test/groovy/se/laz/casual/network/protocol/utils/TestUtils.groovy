/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.utils

import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder

class TestUtils
{
   static roundtripMessage(msg, sink, ProtocolVersion protocolVersion)
    {
       CasualMessageEncoder.write(sink, msg)
       CasualNetworkTestReader.read(sink, protocolVersion)
    }
}
