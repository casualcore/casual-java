/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.work;

import jakarta.resource.spi.work.WorkEvent;
import jakarta.resource.spi.work.WorkListener;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Work Listener to handle completion of {@link jakarta.resource.spi.work.Work} item by
 * {@link jakarta.resource.spi.work.WorkManager} to log if anything goes wrong with starting reverse outbound
 */
public class StartReverseOutboundServerListener implements WorkListener
{
    private static final Logger log = Logger.getLogger( StartReverseOutboundServerListener.class.getName());
    private static final String EXCEPTION_MESSAGE = "Casual reverse outbound start WorkEvent contained an exception: ";
    private StartReverseOutboundServerListener()
    {}

    public static StartReverseOutboundServerListener of()
    {
        return new StartReverseOutboundServerListener();
    }

    @Override
    public void workAccepted(WorkEvent e)
    {
        WorkEventLogger.logWorkEvent( log, e, Level.FINEST, ()->"Casual reverse outbound start, work accepted.", () -> EXCEPTION_MESSAGE );
    }

    @Override
    public void workRejected(WorkEvent e)
    {
        WorkEventLogger.logWorkEvent( log,  e, Level.WARNING, ()-> "Casual reverse outbound start, work rejected, reverse outbound will not be started!!!", () -> EXCEPTION_MESSAGE  );
    }

    @Override
    public void workStarted(WorkEvent e)
    {
        WorkEventLogger.logWorkEvent( log, e, Level.FINEST, ()-> "Casual reverse outbound start, work started.", () -> EXCEPTION_MESSAGE );
    }

    @Override
    public void workCompleted(WorkEvent e)
    {
        WorkEventLogger.logWorkEvent( log, e, Level.FINEST, ()-> "Casual reverse outbound start, work completed.", () -> EXCEPTION_MESSAGE );
    }
}
