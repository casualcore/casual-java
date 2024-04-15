/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test;

import se.laz.casual.event.server.EventServer;
import se.laz.casual.event.server.EventServerConnectionInformation;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class CasualEmbeddedServer
{
    private final int eventServerPort;
    private AtomicBoolean running = new AtomicBoolean( false );
    private EventServer eventServer;

    private CasualEmbeddedServer( int eventServerPort )
    {
        this.eventServerPort = eventServerPort;
    }

    public int getEventServerPort()
    {
        return eventServerPort;
    }

    public boolean isRunning()
    {
        return running.get();
    }

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
        eventServer = EventServer.of(
                EventServerConnectionInformation.createBuilder()
                        .withPort( this.eventServerPort )
                        .build()
        );
    }

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

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        CasualEmbeddedServer that = (CasualEmbeddedServer) o;
        return eventServerPort == that.eventServerPort;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( eventServerPort );
    }

    @Override
    public String toString()
    {
        return "CasualEmbeddedServer{" +
                "eventServerPort=" + eventServerPort +
                '}';
    }

    public static Builder newBuilder( CasualEmbeddedServer src )
    {
        return new Builder().eventServerPort( src.eventServerPort );
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private int eventServerPort;

        public Builder()
        {
        }


        public Builder eventServerPort( int eventServerPort )
        {
            this.eventServerPort = eventServerPort;
            return this;
        }

        public CasualEmbeddedServer build()
        {
            return new CasualEmbeddedServer( eventServerPort );
        }
    }
}
