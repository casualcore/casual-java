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
        synchronized (lock)
        {
            networkListeners.add(listener);
        }
    }

    public void inform()
    {
        synchronized (lock)
        {
            networkListeners.forEach(listener -> listener.disconnected(exception));
            // can only ever inform listeners once even if invoked multiple times
            networkListeners.clear();
        }
    }

}
