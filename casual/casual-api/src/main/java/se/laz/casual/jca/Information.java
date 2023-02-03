/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Information
{
    private final static String INBOUND_SERVER_STARTED = "INBOUND_SERVER_STARTED";
    private final static Map<String, Boolean> information = new ConcurrentHashMap<>();

    private Information()
    {}

    public static boolean isInboundStarted()
    {
        return Optional.ofNullable(information.get(INBOUND_SERVER_STARTED)).orElse(false);
    }

    public static void setInboundStarted(boolean started)
    {
        information.put(INBOUND_SERVER_STARTED, started);
        int i = 0;
    }

}
