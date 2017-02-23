package se.kodarkatten.casual.network.io;


import se.kodarkatten.casual.network.messages.CasualNWMessage;

import java.nio.ByteBuffer;

public final class CasualNetworkReader
{

    public static final CasualNWMessage networkMessageToCasualMessage(final byte[] message) {

        ByteBuffer bytebuffer = ByteBuffer.wrap(message);

        byte[] messagetypeidbyte = new byte[8];
        bytebuffer.get(messagetypeidbyte,0,7);
        int messagetypeid = ByteBuffer.wrap(messagetypeidbyte).getInt();

        return new CasualNWMessage(messagetypeid);
    }

}
