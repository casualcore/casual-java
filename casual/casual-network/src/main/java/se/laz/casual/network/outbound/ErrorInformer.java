package se.laz.casual.network.outbound;

import se.laz.casual.network.connection.CasualConnectionException;

import java.util.List;
import java.util.Objects;

public class ErrorInformer
{
    private final CasualConnectionException exception;
    private final List<NetworkListener> networkListener;
    private final Object lock = new Object();

    private ErrorInformer(CasualConnectionException exception, List<NetworkListener> networkListener)
    {
        this.exception = exception;
        this.networkListener = networkListener;
    }

    public static ErrorInformer of(CasualConnectionException exception, List<NetworkListener> networkListeners)
    {
        Objects.requireNonNull(exception, "exception can not be null");
        Objects.requireNonNull(networkListeners, "networkListeners can not be null");
        return new ErrorInformer(exception, networkListeners);
    }

    public void inform()
    {
        synchronized (lock)
        {
            networkListener.forEach(listener -> listener.disconnected(exception));
        }
    }

}
