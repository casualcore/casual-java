/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.service.casual.discovery;

import se.laz.casual.config.Configuration;
import se.laz.casual.config.Mode;
import se.laz.casual.jca.RuntimeInformation;

import java.util.logging.Logger;

public class TimerStopCondition
{
    private static final Logger log = Logger.getLogger( TimerStopCondition.class.getName());
    private Boolean triggerMode;
    private TimerStopCondition()
    {}

    public static TimerStopCondition of()
    {
        return new TimerStopCondition();
    }

    public boolean stop(Configuration configuration)
    {
        if(null == triggerMode)
        {
            // this does not change at runtime, if inbound is started or not does
            // for testing purposes we cache it here instead of when the class is loaded
            triggerMode = configuration.getInbound().getStartup().getMode() == Mode.TRIGGER;
        }
        log.finest(() -> "startupmode TRIGGER?" + triggerMode);
        log.finest(() -> "RuntimeInformation.isInboundStarted()?" + RuntimeInformation.isInboundStarted());
        return triggerMode && RuntimeInformation.isInboundStarted();
    }
}
