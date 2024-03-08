/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RuntimeInformation
{
    private static final String INBOUND_SERVER_STARTED = "INBOUND_SERVER_STARTED";
    private static final String EVENT_SERVER_STARTED = "EVENT_SERVER_STARTED";
    private static final Map<String, Boolean> CACHE = new ConcurrentHashMap<>();

    private RuntimeInformation()
    {}

    public static boolean isInboundStarted()
    {
        return Optional.ofNullable(CACHE.get(INBOUND_SERVER_STARTED)).orElse(false);
    }

    public static void setInboundStarted(boolean started)
    {
        CACHE.put(INBOUND_SERVER_STARTED, started);
    }

    public static boolean isEventServerStarted()
    {
        return Optional.ofNullable(CACHE.get(EVENT_SERVER_STARTED)).orElse(false);
    }

    public static void setEventServerStarted(boolean started)
    {
        CACHE.put(EVENT_SERVER_STARTED, started);
    }
}
