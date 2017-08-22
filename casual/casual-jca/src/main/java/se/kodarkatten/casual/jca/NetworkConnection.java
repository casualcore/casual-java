package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.network.messages.CasualNWMessage;

/**
 * Created by aleph on 2017-06-14.
 */
public interface NetworkConnection
{
    CasualNWMessage requestReply(CasualNWMessage message);
    void close();
}
