package se.kodarkatten.casual.network.protocol.utils

import se.kodarkatten.casual.network.protocol.io.CasualNetworkReader
import se.kodarkatten.casual.network.protocol.io.CasualNetworkWriter

class TestUtils
{
    static roundtripMessage(msg, sink)
    {
        CasualNetworkWriter.write(sink, msg)
        CasualNetworkReader.read(sink)
    }
}
