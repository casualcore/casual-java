/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.work;

import jakarta.resource.spi.work.WorkEvent;
import jakarta.resource.spi.work.WorkListener;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logs work events emitted while a server starts.
 */
public final class ServerStartupWorkListener implements WorkListener
{
    private static final Logger LOG = Logger.getLogger(ServerStartupWorkListener.class.getName());
    private final String serverType;

    private ServerStartupWorkListener(String serverType)
    {
        this.serverType = Objects.requireNonNull(serverType, "serverType must not be null");
        if(serverType.isBlank())
        {
            throw new IllegalArgumentException("serverType must not be blank");
        }
    }

    /**
     * Creates a listener for the specified server type.
     *
     * @param serverType the server type to include in log messages
     * @return a server-startup work listener
     * @throws NullPointerException if {@code serverType} is {@code null}
     * @throws IllegalArgumentException if {@code serverType} is blank
     */
    public static ServerStartupWorkListener of(String serverType)
    {
        return new ServerStartupWorkListener(serverType);
    }

    @Override
    public void workAccepted(WorkEvent event)
    {
        log(event, Level.FINEST, () -> "work accepted");
    }

    @Override
    public void workRejected(WorkEvent event)
    {
        log(event, Level.WARNING, () -> "work rejected; server does not start");
    }

    @Override
    public void workStarted(WorkEvent event)
    {
        log(event, Level.FINEST, () -> "work started");
    }

    @Override
    public void workCompleted(WorkEvent event)
    {
        log(event, Level.FINEST, () -> "work completed");
    }

    private void log(WorkEvent event, Level level, Supplier<String> message)
    {
        LOG.log(level, () -> "Casual " + serverType + " startup: " + message);
        if(event.getException() != null)
        {
            LOG.log(Level.SEVERE, event.getException(), () -> "Casual " + serverType + " startup work failed");
        }
    }
}
