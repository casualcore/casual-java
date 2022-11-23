/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.conversation.TpConnectReturn;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.Flag;

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
     * AtmiFlags.TPRECVONLY then means that the conversation caller will start receiving data, via tprecv
     *
     * The conversation is only available if {@link TpConnectReturn#getErrorState()} equals to {@link se.laz.casual.api.flags.ErrorState#OK}
     * - if not, you should fail the call with the provided error.
     *
     * Note:
     * Always issue the call using try-with-resources to make sure that nothing leaks
     *
     * @param serviceName - the service name
     * @param data - initial data to be sent
     * @param flags - the flags
     * @return A TpConnectReturn with a conversation if {@link TpConnectReturn#getErrorState()} equals to {@link se.laz.casual.api.flags.ErrorState#OK}
     */
    TpConnectReturn tpconnect(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags);

    /**
     * Same as tpconnect with data but no initial data is sent in this case
     * @param serviceName - the service name
     * @param flags - the flags
     * @return A TpConnectReturn with a conversation if {@link TpConnectReturn#getErrorState()} equals to {@link se.laz.casual.api.flags.ErrorState#OK}
     */
    TpConnectReturn tpconnect(String serviceName, Flag<AtmiFlags> flags);

}
