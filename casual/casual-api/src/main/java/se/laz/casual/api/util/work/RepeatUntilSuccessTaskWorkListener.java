/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.util.work;

import jakarta.resource.spi.work.WorkEvent;
import jakarta.resource.spi.work.WorkListener;
import java.util.logging.Logger;

public class RepeatUntilSuccessTaskWorkListener implements WorkListener
{
    private static Logger log = Logger.getLogger( RepeatUntilSuccessTaskWorkListener.class.getName());

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
        log.warning(() -> "RepeatUntilSuccessTaskWork rejected");
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
            log.warning(() -> "workCompleted failed: " + event.getException());
            log.warning(() -> "cause: " + event.getException().getCause());
        }
    }
}
