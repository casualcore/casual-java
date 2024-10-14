/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import se.laz.casual.config.json.Mode;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;

/**
 * Initialise a ConfigurationStore with the default values for all relevant ConfigurationOptions.
 */
public class ConfigurationDefaults
{
    private final ConfigurationStore store;

    public ConfigurationDefaults( ConfigurationStore store )
    {
        this.store = store;
    }

    /**
     * Populate the ConfigurationStore with the default values.
     */
    public void populate( )
    {
        store.put( ConfigurationOptions.CASUAL_CONFIG_FILE, "" );

        store.put( ConfigurationOptions.CASUAL_API_FIELDED_ENCODING, StandardCharsets.UTF_8.name() );
        store.put( ConfigurationOptions.CASUAL_FIELD_TABLE, null );

        store.put( ConfigurationOptions.CASUAL_DOMAIN_ID, UUID.randomUUID() );
        store.put( ConfigurationOptions.CASUAL_DOMAIN_NAME, "" );

        store.put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE, Mode.IMMEDIATE );
        store.put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_SERVICES, Collections.emptyList() ); //TODO fix this if TRIGGER is used.

        store.put( ConfigurationOptions.CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER, false );
        store.put( ConfigurationOptions.CASUAL_NETWORK_OUTBOUND_ENABLE_LOGHANDLER, false );
        store.put( ConfigurationOptions.CASUAL_NETWORK_REVERSE_INBOUND_ENABLE_LOGHANDLER, false );

        store.put( ConfigurationOptions.CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL, "INFO" );
        store.put( ConfigurationOptions.CASUAL_INBOUND_NETTY_LOGGING_LEVEL, "INFO" );
        store.put( ConfigurationOptions.CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL, "INFO" );

        store.put( ConfigurationOptions.CASUAL_OUTBOUND_USE_EPOLL, false ); //TODO check what default should be.
        store.put( ConfigurationOptions.CASUAL_INBOUND_USE_EPOLL, false ); //TODO check what default should be.
        store.put( ConfigurationOptions.CASUAL_USE_EPOLL, false ); //TODO check what default should be.

        store.put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_INITIAL_DELAY_SECONDS, 0L );
        store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_QUIET_PERIOD_MILLIS, 2000L );
        store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_TIMEOUT_MILLIS, 15000L );

        store.put( ConfigurationOptions.CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE, 10 );
        store.put( ConfigurationOptions.CASUAL_UNMANAGED, false ); //TODO check what default should be.
        store.put( ConfigurationOptions.CASUAL_OUTBOUND_UNMANAGED, false ); //TODO check what default should and if needed with above present.

        store.put( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_NUMBER_OF_THREADS, 0 );
        store.put( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME, "java:comp/DefaultManagedExecutorService" );

        store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_ENABLED, false );
        store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_PORT, 7698 );
        store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_USE_EPOLL, false ); //TODO check what default should be.
        store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_ENABLE_LOGHANDLER, false );

        store.put( ConfigurationOptions.CASUAL_REVERSE_INBOUND_INSTANCES, Collections.emptyList() );
    }
}
