package se.kodarkatten.casual.network.messages;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum CasualNWMessageType
{
    DOMAIN_DISCOVERY_REQUEST(8001),
    DOMAIN_DISCOVERY_REPLY(8002),
    SERVICE_CALL_REQUEST(8003),
    SERVICE_CALL_REPLY(8004);

    private final int messageId;

    //Creating an immutable list of the enum values
    private final static List<CasualNWMessageType> values =
            Collections.unmodifiableList(Arrays.asList(CasualNWMessageType.values()));

    CasualNWMessageType(int messageId)
    {
        this.messageId = messageId;
    }

    public final static CasualNWMessageType unmarshal(int id)
    {
        for (CasualNWMessageType type : CasualNWMessageType.values)
        {
            if (type.messageId == id)
            {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type:" + id);
    }

    public final static int marshal(CasualNWMessageType messageType)
    {
        return messageType.messageId;
    }
}