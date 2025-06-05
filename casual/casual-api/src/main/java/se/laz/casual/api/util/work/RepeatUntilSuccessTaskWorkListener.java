/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.util.work;

import jakarta.resource.spi.work.WorkEvent;
import jakarta.resource.spi.work.WorkListener;

public class RepeatUntilSuccessTaskWorkListener implements WorkListener
{
    private static System.Logger log = System.getLogger( RepeatUntilSuccessTaskWorkListener.class.getName());

    public static RepeatUntilSuccessTaskWorkListener of()
    {
        return new RepeatUntilSuccessTaskWorkListener();
    }

    @Override
    public void workAccepted(WorkEvent e)
    {
        // NOP
    }

    @Override
    public void workRejected(WorkEvent e)
    {
        log.log(System.Logger.Level.WARNING,() -> "RepeatUntilSuccessTaskWork rejected");
    }

    @Override
    public void workStarted(WorkEvent e)
    {
        // NOP
    }

    @Override
    public void workCompleted(WorkEvent event)
    {
        if(null != event.getException())
        {
            log.log(System.Logger.Level.WARNING,() -> "workCompleted failed: " + event.getException());
            log.log(System.Logger.Level.WARNING,() -> "cause: " + event.getException().getCause());
        }
    }
}
