/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import se.laz.casual.api.external.json.JsonProviderFactory;
import se.laz.casual.config.json.Configuration;
import se.laz.casual.config.json.EventServer;
import se.laz.casual.config.json.Inbound;
import se.laz.casual.config.json.Outbound;
import se.laz.casual.config.json.ReverseInbound;
import se.laz.casual.config.json.Shutdown;
import se.laz.casual.config.json.Startup;

import java.io.FileNotFoundException;
import java.io.FileReader;
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
        store.put( ConfigurationOptions.CASUAL_DOMAIN_NAME, configuration.getDomain().getName() );

        populateInbound( configuration.getInbound() );
        populateOutbound( configuration.getOutbound() );
        populateReverseInbound( configuration.getReverseInbound() );

        configuration.getEventServer().ifPresent( this::populateEventServer );

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
        store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_ENABLED, true );
        store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_USE_EPOLL, eventServer.isUseEpoll() );
        store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_PORT, eventServer.getPortNumber() );

        Shutdown shutdown = eventServer.getShutdown();
        if( shutdown != null )
        {
            store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_QUIET_PERIOD_MILLIS,
                    shutdown.getQuietPeriod() );
            store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_TIMEOUT_MILLIS, shutdown.getTimeout() );
        }
    }

    private void populateOutbound( Outbound outbound )
    {
        if( outbound != null )
        {
            store.put( ConfigurationOptions.CASUAL_OUTBOUND_UNMANAGED, outbound.getUnmanaged() );
            store.put( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_NUMBER_OF_THREADS,
                    outbound.getNumberOfThreads() );
            store.put( ConfigurationOptions.CASUAL_OUTBOUND_USE_EPOLL, outbound.getUseEpoll() );
            if( outbound.getManagedExecutorServiceName() != null )
            {
                store.put( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME,
                        outbound.getManagedExecutorServiceName() );
            }
        }
    }

    private void populateInbound( Inbound inbound )
    {
        if( inbound != null )
        {
            populateStartup( inbound.getStartup() );
            store.put( ConfigurationOptions.CASUAL_INBOUND_USE_EPOLL, inbound.isUseEpoll() );
            store.put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_INITIAL_DELAY_SECONDS, inbound.getInitialDelay() );
        }
    }

    private void populateStartup( Startup startup )
    {
        if( startup != null )
        {
            store.put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE, startup.getMode() );
            store.put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_SERVICES, startup.getServices() );
        }
    }

    private void populateReverseInbound( List<ReverseInbound> reverseInbound )
    {
        store.put( ConfigurationOptions.CASUAL_REVERSE_INBOUND_INSTANCES, reverseInbound );
    }

}
