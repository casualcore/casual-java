/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.Flag;

import java.util.Optional;

/**
 * API to start a {@link Conversation}
 */
public interface CasualConversationAPI
{
    /**
     * Flags have to be either:
     * AtmiFlags.TPRECVONLY or AtmiFlags.TPSENDONLY
     *
     * It signifies what state the caller will start out in
     * With AtmiFlags.TPSENDONLY the conversation caller is supposed to start sending data first, via tpsend.
     * AtmiFlags.TPSENDONLY then means that the conversation caller will start receiving data, via tprecv
     *
     * @param serviceName
     * @param data
     * @param flags
     * @return A conversation handle
     */
    Conversation tpconnect(String serviceName, Optional<CasualBuffer> data, Flag<AtmiFlags> flags);
}
