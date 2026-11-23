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
            alreadyInformed = informed;
            if (!alreadyInformed)
            {
                networkListeners.add(listener);
            }
        }
        if (alreadyInformed)
        {
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
        listenersToNotify.forEach(listener -> listener.disconnected(exception));
    }

    public CasualConnectionException getException()
    {
        return exception;
    }
}
