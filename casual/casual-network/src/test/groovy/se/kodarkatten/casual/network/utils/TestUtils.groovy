package se.kodarkatten.casual.network.utils

import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.io.CasualNetworkWriter

class TestUtils
{
    static roundtripMessage(msg, sink)
    {
        CasualNetworkWriter.write(sink, msg)
        CasualNetworkReader.read(sink)
    }
}
