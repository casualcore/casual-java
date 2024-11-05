/*
 * Copyright (c) 2021 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import se.laz.casual.config.json.ConfigurationFileReader;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Singleton access to Casual ConfigurationStore.
 */
public class ConfigurationService
{
    private static final ConfigurationService INSTANCE = new ConfigurationService();

    private ConfigurationStore store;

    ConfigurationService()
    {
        this.store = init();
    }

    private ConfigurationStore init()
    {
        ConfigurationStore newStore = new ConfigurationStore();

        ConfigurationDefaults defaults = new ConfigurationDefaults( newStore );
        defaults.populate();

        ConfigurationEnvsReader envsReader = new ConfigurationEnvsReader( newStore );

        envsReader.populateConfigFileEnv();

        String configurationFile = newStore.get( ConfigurationOptions.CASUAL_CONFIG_FILE );
        if( configurationFile != null && !configurationFile.isBlank() )
        {
            ConfigurationFileReader fileReader = new ConfigurationFileReader( newStore );
            fileReader.populateStoreFromFile( configurationFile );
        }

        envsReader.populateStoreFromEnvs();

        fixInboundStartupServices( newStore );

        fixEpoll( newStore );

        fixUnmanaged( newStore );

        return newStore;
    }

    private void fixUnmanaged( ConfigurationStore store )
    {
        Boolean rootUnmanaged = store.get( ConfigurationOptions.CASUAL_UNMANAGED );
        Boolean outboundUnmanaged = store.get( ConfigurationOptions.CASUAL_OUTBOUND_UNMANAGED );
        if( rootUnmanaged == null )
        {
            rootUnmanaged = outboundUnmanaged;
            store.put( ConfigurationOptions.CASUAL_UNMANAGED, rootUnmanaged );
        }

        if( !rootUnmanaged.equals( outboundUnmanaged ) )
        {
            store.put( ConfigurationOptions.CASUAL_OUTBOUND_UNMANAGED, rootUnmanaged );
        }
    }

    private void fixEpoll( ConfigurationStore store )
    {
        boolean rootEpoll = store.get( ConfigurationOptions.CASUAL_USE_EPOLL );
        if( rootEpoll )
        {
            store.put( ConfigurationOptions.CASUAL_OUTBOUND_USE_EPOLL, true );
            store.put( ConfigurationOptions.CASUAL_INBOUND_USE_EPOLL, true );
        }
    }

    private void fixInboundStartupServices( ConfigurationStore store )
    {
        List<String> services =store.get( ConfigurationOptions.CASUAL_INBOUND_STARTUP_SERVICES );
        services = switch( store.get( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE ) )
        {
            case IMMEDIATE -> Collections.emptyList();
            case TRIGGER -> Collections.singletonList( Mode.Constants.TRIGGER_SERVICE );
            default -> services;
        };
        store.put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_SERVICES, services );
    }

    /**
     * Read only access to the  current configuration value for the requested option.
     *
     * @param option to retrieve.
     * @return current value for the requested option.
     * @param <T> type of the value returned.
     */
    public static <T> T getConfiguration( ConfigurationOption<T> option )
    {
        return INSTANCE.store.get( option );
    }

    /**
     * Mutable access to the current configuration values.
     *
     * @param option to set.
     * @param value to set.
     * @param <T> type of the value to set.
     */
    public static <T> void setConfiguration( ConfigurationOption<T> option, T value )
    {
        INSTANCE.store.put( option, value );
    }

    /**
     * Reload the configuration.
     */
    public static void reload()
    {
        INSTANCE.store = new ConfigurationService().store;
    }

    /**
     * Retrieve the current configuration as a string supplier for use with debugging/logging.
     */
    public static Supplier<String> log( )
    {
        List<ConfigurationOption<?>> options = INSTANCE.store.getData().keySet().stream()
                .sorted( Comparator.comparing( ConfigurationOption::getName ) )
                .toList();

        StringBuilder builder = new StringBuilder();
        for( ConfigurationOption<?> option : options )
        {
            builder.append( option.getName() )
                    .append( " : " )
                    .append( INSTANCE.store.get( option ) )
                    .append( "\n" );
        }
        return builder::toString;
    }
}
