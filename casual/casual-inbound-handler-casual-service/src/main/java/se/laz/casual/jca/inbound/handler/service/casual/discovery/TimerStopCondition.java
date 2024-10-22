/*
 * Copyright (c) 2023 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.service.casual.discovery;

import se.laz.casual.config.Mode;
import se.laz.casual.jca.RuntimeInformation;

import java.util.logging.Logger;

public class TimerStopCondition
{
    private static final Logger log = Logger.getLogger( TimerStopCondition.class.getName());
    private final boolean triggerMode;
    private TimerStopCondition( Mode mode )
    {
        this.triggerMode = mode == Mode.TRIGGER;
    }

    public static TimerStopCondition of( Mode mode )
    {
        return new TimerStopCondition( mode );
    }

    public boolean stop( )
    {
        log.finest(() -> "startupmode TRIGGER?" + triggerMode);
        log.finest(() -> "RuntimeInformation.isInboundStarted()?" + RuntimeInformation.isInboundStarted());
        return triggerMode && RuntimeInformation.isInboundStarted();
    }
}
