/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.test.service.remote;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.Flag;

public interface TpCaller
{
    CasualBuffer makeTpCall(String serviceName, CasualBuffer msg, Flag<AtmiFlags> flags);
}
