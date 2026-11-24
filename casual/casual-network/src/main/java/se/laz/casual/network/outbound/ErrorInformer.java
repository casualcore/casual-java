/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound;

import se.laz.casual.network.connection.CasualConnectionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ErrorInformer
{
    private final CasualConnectionException exception;
    private final List<NetworkListener> networkListeners = new ArrayList<>();
    private final Object lock = new Object();
    private boolean informed;

    private ErrorInformer(CasualConnectionException exception)
    {
        this.exception = exception;
    }

    public static ErrorInformer of(CasualConnectionException exception)
    {
        Objects.requireNonNull(exception, "exception can not be null");
        return new ErrorInformer(exception);
    }

    public void addListener(NetworkListener listener)
    {
        Objects.requireNonNull(listener, "listener can not be null");
        boolean alreadyInformed;
        synchronized (lock)
        {
            // If inform() already ran (for example, an accepted reverse-outbound channel dropped
            // before pool registration), notify this listener immediately.
            alreadyInformed = informed;
            if (!alreadyInformed)
            {
                networkListeners.add(listener);
            }
        }
        if (alreadyInformed)
        {
            // Notify outside lock to avoid lock-ordering deadlocks with callers such as NetworkConnectionPool.
            listener.disconnected(exception);
        }
    }

    public void inform()
    {
        List<NetworkListener> listenersToNotify;
        synchronized (lock)
        {
            if (informed)
            {
                return;
            }
            informed = true;
            listenersToNotify = List.copyOf(networkListeners);
            // can only ever inform listeners once even if invoked multiple times
            networkListeners.clear();
        }
        // Notify outside lock so listeners that acquire pool locks cannot deadlock with addListener().
        listenersToNotify.forEach(listener -> listener.disconnected(exception));
    }

    public CasualConnectionException getException()
    {
        return exception;
    }
}
