/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.work;

import jakarta.resource.spi.work.WorkEvent;
import jakarta.resource.spi.work.WorkListener;
import static java.lang.System.Logger.Level.*;

/**
 * Work Listener to handle completion of {@link jakarta.resource.spi.work.Work} item by
 * {@link jakarta.resource.spi.work.WorkManager} to log if anything goes wrong with starting reverse inbound
 */
public class StartReverseInboundServerListener implements WorkListener
{
    private static System.Logger log = System.getLogger( StartReverseInboundServerListener.class.getName());
    private StartReverseInboundServerListener()
    {
    }

    public static StartReverseInboundServerListener of()
    {
        return new StartReverseInboundServerListener();
    }

    @Override
    public void workAccepted(WorkEvent e)
    {
        //No Op
    }

    @Override
    public void workRejected(WorkEvent e)
    {
        log.log(WARNING,() -> "reverse inbound workRejected, reverse inbound will not be started!!!",e.getException());
    }

    @Override
    public void workStarted(WorkEvent e)
    {
        //No Op
    }

    @Override
    public void workCompleted(WorkEvent e)
    {
        if(null != e.getException())
        {
            log.log(WARNING,() -> "reverse inbound, workCompleted with exception: " + e.getException(),e.getException());
            log.log(WARNING,() -> "cause: " + e.getException().getCause(),e.getException());
        }
    }
}
