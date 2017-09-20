package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;

/**
 * Created by aleph on 2017-06-14.
 */
public interface NetworkConnection
{
    <T extends CasualNetworkTransmittable,X extends CasualNetworkTransmittable> CasualNWMessage<T> requestReply(CasualNWMessage<X> message);
    void close();
}
