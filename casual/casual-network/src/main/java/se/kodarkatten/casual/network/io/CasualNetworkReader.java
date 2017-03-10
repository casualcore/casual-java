package se.kodarkatten.casual.network.io;


import se.kodarkatten.casual.network.io.readers.CasualDomainDiscoveryReplyMessageReader;
import se.kodarkatten.casual.network.io.readers.CasualDomainDiscoveryRequestMessageReader;
import se.kodarkatten.casual.network.io.readers.CasualNWMessageHeaderReader;
import se.kodarkatten.casual.network.messages.CasualNWMessageHeader;
import se.kodarkatten.casual.network.messages.reply.CasualDomainDiscoveryReplyMessage;
import se.kodarkatten.casual.network.messages.request.CasualDomainDiscoveryRequestMessage;

import java.util.List;

public final class CasualNetworkReader
{
    private CasualNetworkReader()
    {
        //no instances should be crated
    }

    public static CasualNWMessageHeader networkHeaderToCasualHeader(final byte[] message)
    {
        return CasualNWMessageHeaderReader.fromNetworkBytes(message);
    }

    /**
     * When reading the bytes - if the payload in the header is less than Integer.MAX_VALUE
     * then this list of bytes should only contain one byte[]
     * If not, list should contain one byte[] per member of
     * message::interdomain::domain::discovery::Request that contain dynamic arrays
     * Other data may be packed into one byte[] but the format is not yet settled
     * @param payload
     * @return
     */
    public static CasualDomainDiscoveryRequestMessage networkDomainDiscoveryRequestToCasualDomainDiscoveryRequestMessage(final List<byte[]> payload)
    {
        return CasualDomainDiscoveryRequestMessageReader.fromNetworkBytes(payload);
    }

    public static CasualDomainDiscoveryReplyMessage networkDomainDiscoverReplyToCasualDomainDiscoveryReplyMessage(final List<byte[]> payload)
    {
        return CasualDomainDiscoveryReplyMessageReader.fromNetworkBytes(payload);
    }



}
