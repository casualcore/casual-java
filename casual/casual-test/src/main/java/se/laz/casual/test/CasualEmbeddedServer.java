/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test;

import se.laz.casual.event.ServiceCallEvent;
import se.laz.casual.event.ServiceCallEventStore;
import se.laz.casual.event.ServiceCallEventStoreFactory;
import se.laz.casual.event.server.EventServer;
import se.laz.casual.event.server.EventServerConnectionInformation;
import se.laz.casual.test.network.NetworkPortFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A light-weight instance of a casual server, initially focused on integration testing.
 *
 * NB - Currently provides just a casual event server interface.
 */
public class CasualEmbeddedServer
{
    private final UUID domainId;
    private final Integer eventServerPort;
    private final boolean eventServerEnabled;
    private AtomicBoolean running = new AtomicBoolean( false );
    private AtomicBoolean eventServerRunning = new AtomicBoolean( false );
    private EventServer eventServer;

    private CasualEmbeddedServer( Builder builder )
    {
        this.domainId = UUID.randomUUID();
        this.eventServerPort = builder.eventServerPort;
        this.eventServerEnabled = builder.eventServerEnabled;
    }

    /**
     * Get the port for the event server instance.
     *
     * @return optional populated with port if provided otherwise empty.
     */
    public Optional<Integer> getEventServerPort()
    {
        return Optional.ofNullable( eventServerPort );
    }

    /**
     * Is the event server enabled?
     *
     * @return if event server is enabled.
     */
    public boolean isEventServerEnabled()
    {
        return eventServerEnabled;
    }

    /**
     * Is the event server currently running?
     *
     * @return if event server is running.
     */
    public boolean isEventServerRunning()
    {
        return eventServerRunning.get();
    }

    /**
     * Is the casual embedded server running?
     *
     * @return if the embedded server is running.
     */
    public boolean isRunning()
    {
        return running.get();
    }

    /**
     * Start the casual embedded server.
     *
     * NB - can only be called when the server is not running.
     */
    public void start()
    {
        if( running.compareAndSet( false, true ) )
        {
            initialiseEventServer();
            return;
        }
        throw new IllegalStateException( "Server is already started." );
    }

    private void initialiseEventServer()
    {
        if( !this.eventServerEnabled )
        {
            return;
        }
        eventServer = EventServer.of(
                EventServerConnectionInformation.createBuilder()
                        .withPort( this.eventServerPort )
                        .withLogHandlerEnabled( true )
                        .build(),
                this.domainId
        );
        eventServerRunning.set( true );
    }

    /**
     * Stop the currently running casual embedded server.
     *
     * NB - Can be called multiple times, though only has effect when server is currently running.
     */
    public void shutdown()
    {
        if( running.compareAndSet( true, false ) )
        {
            shutdownEventServer();
        }
    }

    private void shutdownEventServer()
    {
        if( eventServer != null )
        {
            eventServer.close();
        }
    }

    /**
     * Simulate a service call event being published.
     *
     * Can be used to test integration with event server.
     *
     * @param serviceCallEvent to be published.
     */
    public void publishEvent(ServiceCallEvent serviceCallEvent)
    {
        ServiceCallEventStore store = ServiceCallEventStoreFactory.getStore(this.domainId);
        store.put(serviceCallEvent);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CasualEmbeddedServer that = (CasualEmbeddedServer) o;
        return eventServerPort == that.eventServerPort && eventServerEnabled == that.eventServerEnabled && Objects.equals(running, that.running);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(eventServerPort, eventServerEnabled, running);
    }

    @Override
    public String toString()
    {
        return "CasualEmbeddedServer{" +
                "eventServerPort=" + eventServerPort +
                ", eventServerEnabled=" + eventServerEnabled +
                ", running=" + running +
                '}';
    }

    public static Builder newBuilder(CasualEmbeddedServer src )
    {
        return new Builder().eventServerPort( src.eventServerPort );
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private boolean eventServerEnabled = false;
        private Integer eventServerPort;

        public Builder()
        {
        }


        public Builder eventServerPort( Integer eventServerPort )
        {
            this.eventServerPort = eventServerPort;
            return this;
        }

        public Builder eventServerEnabled( boolean enabled )
        {
            this.eventServerEnabled = enabled;
            return this;
        }

        public CasualEmbeddedServer build()
        {
            if( eventServerEnabled && eventServerPort == null )
            {
                eventServerPort = NetworkPortFactory.getAvailablePort();
            }
            return new CasualEmbeddedServer( this );
        }
    }
}
