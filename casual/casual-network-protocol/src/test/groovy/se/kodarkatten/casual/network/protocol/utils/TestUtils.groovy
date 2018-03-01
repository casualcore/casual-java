package se.kodarkatten.casual.network.protocol.utils

import se.kodarkatten.casual.network.protocol.decoding.CasualNetworkTestReader
import se.kodarkatten.casual.network.protocol.encoding.CasualMessageEncoder

class TestUtils
{
    static roundtripMessage(msg, sink)
    {
        CasualMessageEncoder.write(sink, msg)
        CasualNetworkTestReader.read(sink)
    }
}
