/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static se.laz.casual.api.buffer.type.fielded.Constants.CASUAL_FIELD_JSON_EMBEDDED;

/**
 * Store for all configuration options.
 */
public class ConfigurationStore
{
    private final Map<ConfigurationOption<?>, Object> data = new HashMap<>();

    public ConfigurationStore()
    {
        initialiseDefaults();
    }

    private void initialiseDefaults()
    {
        put( ConfigurationOptions.CASUAL_CONFIG_FILE, "" );

        put( ConfigurationOptions.CASUAL_API_FIELDED_ENCODING, "UTF8" );
        put( ConfigurationOptions.CASUAL_FIELD_TABLE, CASUAL_FIELD_JSON_EMBEDDED );

        put( ConfigurationOptions.CASUAL_DOMAIN_ID, UUID.randomUUID() );
        put( ConfigurationOptions.CASUAL_DOMAIN_NAME, "" );

        put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE, Mode.IMMEDIATE );
        put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_SERVICES, Collections.emptyList() ); //TODO fix this if TRIGGER is used.

        put( ConfigurationOptions.CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER, false );
        put( ConfigurationOptions.CASUAL_NETWORK_OUTBOUND_ENABLE_LOGHANDLER, false );
        put( ConfigurationOptions.CASUAL_NETWORK_REVERSE_INBOUND_ENABLE_LOGHANDLER, false );

        put( ConfigurationOptions.CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL, "INFO" );
        put( ConfigurationOptions.CASUAL_INBOUND_NETTY_LOGGING_LEVEL, "INFO" );
        put( ConfigurationOptions.CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL, "INFO" );

        put( ConfigurationOptions.CASUAL_OUTBOUND_USE_EPOLL, false ); //TODO check what default should be.
        put( ConfigurationOptions.CASUAL_INBOUND_USE_EPOLL, false ); //TODO check what default should be.
        put( ConfigurationOptions.CASUAL_USE_EPOLL, false ); //TODO check what default should be.

        put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_INITIAL_DELAY_SECONDS, 0L );
        put( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_QUIET_PERIOD_MILLIS, 2000 );
        put( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_TIMEOUT_MILLIS, 15000 );

        put( ConfigurationOptions.CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE, 10 );
        put( ConfigurationOptions.CASUAL_UNMANAGED, false ); //TODO check what default should be.

        put( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_NUMBER_OF_THREADS, 0 );
        put( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME, "java:comp/DefaultManagedExecutorService" );

        put( ConfigurationOptions.CASUAL_EVENT_SERVER_PORT, 7698 );
        put( ConfigurationOptions.CASUAL_EVENT_SERVER_USE_EPOLL, false ); //TODO check what default should be.

        put( ConfigurationOptions.CASUAL_REVERSE_INBOUND_INSTANCES, Collections.emptyList() );


    }

    /**
     * Set value for a configuration option, ensuring that value types placed into the store are of the correct type.
     *
     * @param option Configuration option to set.
     * @param value Value to set configuration option to.
     * @param <T> type of value the configuration option allows.
     */
    public <T> void put( ConfigurationOption<T> option, T value )
    {
        data.put( option, value );
    }

    /**
     * Retrieve value for configuration option provided.
     *
     * @param option configuration option to retrieve.
     * @return value of stored configuration option.
     * @param <T> type of the configuration option value.
     */
    // Values are only stored with the right type and type erasure prevents prior check with instanceof.
    @SuppressWarnings( "unchecked" )
    public <T> T get( final ConfigurationOption<T> option )
    {
        return (T) data.get( option );
    }

}
