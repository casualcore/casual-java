/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.conversation;

import java.util.UUID;

@FunctionalInterface
public interface ConversationClose
{
    void close(UUID conversationalCorrId);
}
