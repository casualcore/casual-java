/*
 * Copyright (c) 2023 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.service.casual.discovery;

import se.laz.casual.config.Mode;
import se.laz.casual.jca.RuntimeInformation;

public class TimerStopCondition
{
    private static final System.Logger log = System.getLogger( TimerStopCondition.class.getName());
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
        log.log(System.Logger.Level.TRACE,() -> "startupmode TRIGGER?" + triggerMode);
        log.log(System.Logger.Level.TRACE,() -> "RuntimeInformation.isInboundStarted()?" + RuntimeInformation.isInboundStarted());
        return triggerMode && RuntimeInformation.isInboundStarted();
    }
}
