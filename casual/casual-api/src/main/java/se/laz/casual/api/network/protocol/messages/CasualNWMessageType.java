/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.network.protocol.messages;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The possible message types when talking to casual
 */
public enum CasualNWMessageType
{
    DOMAIN_CONNECT_REQUEST(7200),
    DOMAIN_CONNECT_REPLY(7201),
    DOMAIN_DISCONNECT_REQUEST(7202),
    DOMAIN_DISCONNECT_REPLY(7203),
    DOMAIN_DISCOVERY_REQUEST(7300),
    DOMAIN_DISCOVERY_REPLY(7301),
    SERVICE_CALL_REQUEST(3100),
    SERVICE_CALL_REPLY(3101),
    ENQUEUE_REQUEST(6100),
    ENQUEUE_REPLY(6101),
    DEQUEUE_REQUEST(6200),
    DEQUEUE_REPLY(6201),
    PREPARE_REQUEST(5201),
    PREPARE_REQUEST_REPLY(5202),
    COMMIT_REQUEST(5203),
    COMMIT_REQUEST_REPLY(5204),
    REQUEST_ROLLBACK(5205),
    REQUEST_ROLLBACK_REPLY(5206),
    CONVERSATION_CONNECT(3210),
    CONVERSATION_CONNECT_REPLY(3211),
    CONVERSATION_REQUEST(3212),
    CONVERSATION_DISCONNECT(3213);

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