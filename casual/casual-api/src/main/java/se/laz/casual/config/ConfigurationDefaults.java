/*
 * Copyright (c) 2024 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import se.laz.casual.jca.DomainId;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Initialise a ConfigurationStore with the default values for all relevant ConfigurationOptions.
 */
public class ConfigurationDefaults
{
    public static final String ENCODING_DEFAULT = StandardCharsets.UTF_8.name();
    public static final DomainId DOMAIN_ID_DEFAULT = DomainId.of( UUID.randomUUID() );
    public static final Mode INBOUND_STARTUP_MODE_DEFAULT = Mode.IMMEDIATE;
    public static final List<String> INBOUND_START_SERVICES_DEFAULT = Collections.emptyList();
    public static final Boolean NETWORK_ENABLE_LOGHANDLER_DEFAULT = false;
    public static final String NETTY_LOGGING_LEVEL_DEFAULT = "INFO";
    public static final Boolean USE_EPOLL_DEFAULT = false;
    public static final Long INBOUND_STARTUP_INITIAL_DELAY_DEFAULT = 0L;
    public static final Long SHUTDOWN_QUIET_PERIOD_DEFAULT = 2000L;
    public static final Long SHUTDOWN_TIMEOUT_DEFAULT = 15000L;
    public static final Integer UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE_DEFAULT = 10;
    public static final Boolean UNMANAGED_DEFAULT = false;
    public static final Integer EXECUTOR_NUMBER_OF_THREADS_DEFAULT = 0;
    public static final String OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME_DEFAULT = "java:comp/DefaultManagedExecutorService";
    public static final Boolean EVENT_SERVER_ENABLED_DEFAULT = false;
    public static final Integer EVENT_SERVER_PORT_DEFAULT = 7698;
    public static final List<ReverseInbound> REVERSE_INBOUND_INSTANCES_DEFAULT = Collections.emptyList();
    public static final List<ReverseOutbound> REVERSE_OUTBOUND_INSTANCES_DEFAULT = Collections.emptyList();
    public static final Integer REVERSE_INBOUND_CONNECTION_POOL_SIZE_DEFAULT = 1;
    public static final Long REVERSE_INBOUND_CONNECTION_MAX_BACKOFF_DEFAULT = 30000L;
    public static final String CONFIG_FILE_DEFAULT = "";
    public static final String DOMAIN_NAME_DEFAULT = "";

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
        store.put( ConfigurationOptions.CASUAL_CONFIG_FILE, CONFIG_FILE_DEFAULT );

        store.put( ConfigurationOptions.CASUAL_API_FIELDED_ENCODING, ENCODING_DEFAULT );

        store.put( ConfigurationOptions.CASUAL_DOMAIN_ID, DOMAIN_ID_DEFAULT );
        store.put( ConfigurationOptions.CASUAL_DOMAIN_NAME, DOMAIN_NAME_DEFAULT );

        store.put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE, INBOUND_STARTUP_MODE_DEFAULT );
        store.put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_SERVICES, INBOUND_START_SERVICES_DEFAULT );

        store.put( ConfigurationOptions.CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER, NETWORK_ENABLE_LOGHANDLER_DEFAULT );
        store.put( ConfigurationOptions.CASUAL_NETWORK_OUTBOUND_ENABLE_LOGHANDLER, NETWORK_ENABLE_LOGHANDLER_DEFAULT );
        store.put( ConfigurationOptions.CASUAL_NETWORK_REVERSE_INBOUND_ENABLE_LOGHANDLER, NETWORK_ENABLE_LOGHANDLER_DEFAULT );

        store.put( ConfigurationOptions.CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL, NETTY_LOGGING_LEVEL_DEFAULT );
        store.put( ConfigurationOptions.CASUAL_INBOUND_NETTY_LOGGING_LEVEL, NETTY_LOGGING_LEVEL_DEFAULT );
        store.put( ConfigurationOptions.CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL, NETTY_LOGGING_LEVEL_DEFAULT );

        store.put( ConfigurationOptions.CASUAL_OUTBOUND_USE_EPOLL, USE_EPOLL_DEFAULT );
        store.put( ConfigurationOptions.CASUAL_INBOUND_USE_EPOLL, USE_EPOLL_DEFAULT );
        store.put( ConfigurationOptions.CASUAL_USE_EPOLL, USE_EPOLL_DEFAULT );

        store.put( ConfigurationOptions.CASUAL_INBOUND_STARTUP_INITIAL_DELAY_SECONDS, INBOUND_STARTUP_INITIAL_DELAY_DEFAULT );
        store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_QUIET_PERIOD_MILLIS, SHUTDOWN_QUIET_PERIOD_DEFAULT );
        store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_TIMEOUT_MILLIS, SHUTDOWN_TIMEOUT_DEFAULT );

        store.put( ConfigurationOptions.CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE, UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE_DEFAULT );

        store.put( ConfigurationOptions.CASUAL_OUTBOUND_UNMANAGED, UNMANAGED_DEFAULT );

        store.put( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_NUMBER_OF_THREADS, EXECUTOR_NUMBER_OF_THREADS_DEFAULT );
        store.put( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME, OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME_DEFAULT );

        store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_ENABLED, EVENT_SERVER_ENABLED_DEFAULT );
        store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_PORT, EVENT_SERVER_PORT_DEFAULT );
        store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_USE_EPOLL, USE_EPOLL_DEFAULT );
        store.put( ConfigurationOptions.CASUAL_EVENT_SERVER_ENABLE_LOGHANDLER, NETWORK_ENABLE_LOGHANDLER_DEFAULT );

        store.put( ConfigurationOptions.CASUAL_REVERSE_INBOUND_INSTANCES, REVERSE_INBOUND_INSTANCES_DEFAULT );
        store.put( ConfigurationOptions.CASUAL_REVERSE_OUTBOUND_INSTANCES, REVERSE_OUTBOUND_INSTANCES_DEFAULT );
        store.put( ConfigurationOptions.CASUAL_REVERSE_INBOUND_INSTANCE_CONNECTION_POOL_SIZE, REVERSE_INBOUND_CONNECTION_POOL_SIZE_DEFAULT );
        store.put( ConfigurationOptions.CASUAL_REVERSE_INBOUND_INSTANCE_CONNECTION_MAX_BACKOFF_MILLIS, REVERSE_INBOUND_CONNECTION_MAX_BACKOFF_DEFAULT );
    }
}
