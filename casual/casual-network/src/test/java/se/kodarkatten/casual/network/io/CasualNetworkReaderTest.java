package se.kodarkatten.casual.network.io;

import org.junit.Test;
import se.kodarkatten.casual.network.messages.CasualNWMessageHeader;
import se.kodarkatten.casual.network.messages.CasualNWMessageType;
import se.kodarkatten.casual.network.messages.parseinfo.MessageHeaderSizes;

import java.nio.ByteBuffer;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author jone
 */
public class CasualNetworkReaderTest
{
    @Test
    public void parseBytesToMessage()
    {
        ByteBuffer b = ByteBuffer.allocate(MessageHeaderSizes.getHeaderNetworkSize());
        final UUID correlationId = UUID.randomUUID();
        final int payloadSize = Integer.MAX_VALUE;
        b.putLong(CasualNWMessageType.DOMAIN_DISCOVERY_REQUEST.getMessageId())
         .putLong(correlationId.getMostSignificantBits())
         .putLong(correlationId.getLeastSignificantBits())
         .putLong(payloadSize);
        CasualNWMessageHeader casualMessage = CasualNetworkReader.networkHeaderToCasualHeader(b.array());
        assertEquals("Checking that the message type id of the unmarshaled message has the correct value", CasualNWMessageType.DOMAIN_DISCOVERY_REQUEST, casualMessage.getType());
        assertEquals("Checking that correlationId is correct", correlationId, casualMessage.getCorrelationId());
        assertEquals("Checking payload size", payloadSize, casualMessage.getPayloadSize());
    }

}