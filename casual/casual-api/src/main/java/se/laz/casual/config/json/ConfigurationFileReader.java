/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json;

import se.laz.casual.api.external.json.JsonProviderFactory;
import se.laz.casual.config.ConfigurationException;
import se.laz.casual.config.ConfigurationOptions;
import se.laz.casual.config.ConfigurationStore;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Read through the configuration file and envs and populate the configuration store with the appropriate values.
 */
public class ConfigurationFileReader
{
    private final ConfigurationStore store;

    public ConfigurationFileReader( ConfigurationStore store )
    {
        this.store = store;
    }

    /**
     * Populate from the provided file.
     *
     * @param filename
     */
    public void populateStoreFromFile( String filename )
    {
        Configuration configuration = readFile( filename );

        populateDomain( configuration.getDomain() );
        populateInbound( configuration.getInbound() );
        populateOutbound( configuration.getOutbound() );
        populateReverseInbound( configuration.getReverseInbound() );
        populateEventServer( configuration.getEventServer() );
    }

    private void populateDomain( Domain domain )
    {
        if( domain != null && domain.getName() != null )
        {
            store.put( ConfigurationOptions.CASUAL_DOMAIN_NAME, domain.getName() );
        }
    }

    private static Configuration readFile( String filename )
    {
        try
        {
            return JsonProviderFactory.getJsonProvider().fromJson( new FileReader( filename ), Configuration.class );
        }
        catch( FileNotFoundException e )
        {
            throw new ConfigurationException( "Could not find configuration file specified.", e );
        }
    }

    private void populateEventServer( EventServer eventServer )
    {
        if( eventServer == null )
        {
            return;
        }
        store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_ENABLED, true );

        if( eventServer.getUseEpoll() != null )
        {
            store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_USE_EPOLL, eventServer.getUseEpoll() );
        }
        if( eventServer.getPortNumber() != null )
        {
            store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_PORT, eventServer.getPortNumber() );
        }

        Shutdown shutdown = eventServer.getShutdown();
        if( shutdown != null )
        {
            if( shutdown.getQuietPeriod() != null )
            {
                store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_QUIET_PERIOD_MILLIS,
                        shutdown.getQuietPeriod() );
            }
            if( shutdown.getTimeout() != null )
            {
                store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_TIMEOUT_MILLIS, shutdown.getTimeout() );
            }
        }
    }

    private void populateOutbound( Outbound outbound )
    {
        if( outbound == null )
        {
            return;
        }

        if( outbound.getUnmanaged() != null )
        {
            store.put( ConfigurationOptions.CASUAL_OUTBOUND_UNMANAGED, outbound.getUnmanaged() );
        }
        if( outbound.getNumberOfThreads() != null )
        {
            store.put( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_NUMBER_OF_THREADS,
                    outbound.getNumberOfThreads() );
        }
        if( outbound.getUseEpoll() != null )
        {
            store.put( ConfigurationOptions.CASUAL_OUTBOUND_USE_EPOLL, outbound.getUseEpoll() );
        }

        if( outbound.getManagedExecutorServiceName() != null )
        {
            store.put( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME,
                    outbound.getManagedExecutorServiceName() );
        }
    }

    private void populateInbound( Inbound inbound )
    {
        if( inbound == null )
        {
            return;
        }

        if( inbound.getUseEpoll() != null )
        {
            store.put( ConfigurationOptions.CASUAL_INBOUND_USE_EPOLL, inbound.getUseEpoll() );
        }
        if( inbound.getInitialDelay() != null )
        {
            store.put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_INITIAL_DELAY_SECONDS, inbound.getInitialDelay() );
        }

        populateStartup( inbound.getStartup() );

    }

    private void populateStartup( Startup startup )
    {
        if( startup == null )
        {
            return;
        }

        if( startup.getMode() != null )
        {
            se.laz.casual.config.Mode m = se.laz.casual.config.Mode.fromName( startup.getMode().getName() );
            store.put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE, m );
        }

        if( startup.getServices() != null )
        {
            store.put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_SERVICES, startup.getServices() );
        }
    }

    private void populateReverseInbound( List<ReverseInbound> reverseInbound )
    {
        if( reverseInbound == null )
        {
            return;
        }
        List<se.laz.casual.config.ReverseInbound> updated = new ArrayList<>(reverseInbound.size());
        for( ReverseInbound inbound : reverseInbound )
        {
            se.laz.casual.config.ReverseInbound.Builder update = se.laz.casual.config.ReverseInbound.newBuilder( );
            if( inbound.getAddress() != null )
            {
                update.withHost( inbound.getAddress().getHost() )
                        .withPort( inbound.getAddress().getPort() );

            }

            update.withSize( inbound.getSize() != null ?
                    inbound.getSize() :
                    store.get( ConfigurationOptions.CASUAL_REVERSE_INBOUND_INSTANCE_CONNECTION_POOL_SIZE  ) );

            update.withMaxConnectionBackoffMillis( inbound.getMaxConnectionBackoffMillis() != null ?
                    inbound.getMaxConnectionBackoffMillis() :
                    store.get( ConfigurationOptions.CASUAL_REVERSE_INBOUND_INSTANCE_CONNECTION_MAX_BACKOFF_MILLIS ) );

            updated.add( update.build() );
        }
        store.put( ConfigurationOptions.CASUAL_REVERSE_INBOUND_INSTANCES, updated );
    }

}
