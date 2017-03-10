package se.kodarkatten.casual.network.messages;

import java.util.List;

/**
 * Created by aleph on 2017-03-09.
 */
public interface CasualNetworkTransmittable
{
    CasualNWMessageType getType();
    List<byte[]> toNetworkBytes();
}
