package se.kodarkatten.casual.jca.event;

import se.kodarkatten.casual.network.protocol.connection.CasualConnectionException;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class ConnectionEventHandler
{
    private static final Logger logger = Logger.getLogger(ConnectionEventHandler.class.getName());

    private final List<javax.resource.spi.ConnectionEventListener> listeners;

    public ConnectionEventHandler()
    {
        this.listeners = Collections.synchronizedList(new ArrayList<ConnectionEventListener>(1));
    }

    /**
     * Adds a connection event listener to the ManagedConnection instance.
     *
     * @param listener A new ConnectionEventHandler to be registered
     */
    public void addConnectionEventListener(ConnectionEventListener listener)
    {
        logger.finest("addConnectionEventListener()");
        Objects.requireNonNull( listener, "Listener is null" );
        listeners.add(listener);
    }

    /**
     * Number of currently registered listeners.
     *
     * @return number of registered listeners.
     */
    public int listenerCount()
    {
        return listeners.size();
    }

    /**
     * Removes an already registered connection event listener from the ManagedConnection instance.
     *
     * @param listener already registered connection event listener to be removed
     */
    public void removeConnectionEventListener(ConnectionEventListener listener)
    {
        logger.finest("removeConnectionEventListener()");
        Objects.requireNonNull( listener, "Listener is null" );
        listeners.remove(listener);
    }

    public void sendEvent(ConnectionEvent event)
    {
        List<javax.resource.spi.ConnectionEventListener> copy = new ArrayList<>();
        synchronized (listeners)
        {
            copy.addAll(listeners);
        }
        for (ConnectionEventListener l : copy)
        {
            switch (event.getId())
            {
                case ConnectionEvent.CONNECTION_CLOSED:
                    l.connectionClosed(event);
                    break;
                case ConnectionEvent.CONNECTION_ERROR_OCCURRED:
                    l.connectionErrorOccurred(event);
                    break;
                case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
                    l.localTransactionCommitted(event);
                    break;
                case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
                    l.localTransactionRolledback(event);
                    break;
                case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
                    l.localTransactionStarted(event);
                    break;
                default:
                    // TODO:
                    // maybe not throw, just ignore?
                    throw new CasualConnectionException("unkown event:" + event);
            }
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        ConnectionEventHandler that = (ConnectionEventHandler) o;
        return Objects.equals(listeners, that.listeners);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(listeners);
    }

    @Override
    public String toString()
    {
        return "ConnectionEventHandler{" +
                "listeners=" + listeners +
                '}';
    }
}
