/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;

import se.laz.casual.api.CasualConversationAPI;
import se.laz.casual.api.CasualQueueApi;
import se.laz.casual.api.CasualServiceApi;
import se.laz.casual.api.Conversation;
import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.Flag;

import java.util.Optional;

/**
 * NetworkConnection handle used in the application to call Casual Services.
 *
 * @version $Revision: $
 */
public interface CasualConnection extends CasualServiceApi, CasualQueueApi, CasualConversationAPI, AutoCloseable
{
    /**
     * Clean up the connection handle and close.
     */
    @Override
    void close();
}
