package se.kodarkatten.casual.network.io;

import org.junit.Test;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.CasualNWMessageType;

import java.nio.ByteBuffer;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author jone
 */
public class CasualNetworkReaderTest
{
    @Test
    public void parseBytesToMessage() {
        byte[] message = ByteBuffer.allocate(1024).putInt(8001).put(UUID.randomUUID().toString().getBytes()).array();

        CasualNWMessage casualMessage = CasualNetworkReader.networkMessageToCasualMessage(message);


        assertEquals("Checking that the message type id of the unmashaled message has the correct value", 8001, CasualNWMessageType.marshal(casualMessage.getType()));
    }

}