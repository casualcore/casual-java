/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.work;

import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkListener;
import java.util.logging.Logger;

/**
 * Work Listener to handle completion of {@link javax.resource.spi.work.Work} item by
 * {@link javax.resource.spi.work.WorkManager} to log if anything goes wrong with starting reverse inbound
 */
public class StartReverseInboundServerListener implements WorkListener
{
    private static Logger log = Logger.getLogger( StartReverseInboundServerListener.class.getName());
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
        log.warning(() -> "reverse inbound workRejected, reverse inbound will not be started!!!");
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
            log.warning(() -> "reverse inbound, workCompleted with exception: " + e.getException());
            log.warning(() -> "cause: " + e.getException().getCause());
        }
    }
}
