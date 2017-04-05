package se.kodarkatten.casual.network.messages;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum CasualNWMessageType
{
    DOMAIN_DISCOVERY_REQUEST(8001),
    DOMAIN_DISCOVERY_REPLY(8002),
    SERVICE_CALL_REQUEST(8100),
    SERVICE_CALL_REPLY(8101),
    PREPARE_REQUEST(8300),
    PREPARE_REQUEST_REPLY(8301),
    COMMIT_REQUEST(8302),
    COMMIT_REQUEST_REPLY(8303),
    REQUEST_ROLLBACK(8304),
    REQUEST_ROLLBACK_REPLY(8305);

    private final int messageId;

    //Creating an immutable list of the enum values
    private static final List<CasualNWMessageType> values =
            Collections.unmodifiableList(Arrays.asList(CasualNWMessageType.values()));

    CasualNWMessageType(int messageId)
    {
        this.messageId = messageId;
    }

    public static final CasualNWMessageType unmarshal(int id)
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

    public int getMessageId()
    {
        return messageId;
    }

    public static final int marshal(CasualNWMessageType messageType)
    {
        return messageType.messageId;
    }
}