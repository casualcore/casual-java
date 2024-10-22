/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import java.util.List;
import java.util.UUID;

/**
 * All available configuration options.
 */
public class ConfigurationOptions
{
    public static final ConfigurationOption<String> CASUAL_API_FIELDED_ENCODING = new ConfigurationOption<>( "CASUAL_API_FIELDED_ENCODING" );
    public static final ConfigurationOption<String> CASUAL_FIELD_TABLE = new ConfigurationOption<>( "CASUAL_FIELD_TABLE" );
    public static final ConfigurationOption<Boolean> CASUAL_NETWORK_OUTBOUND_ENABLE_LOGHANDLER = new ConfigurationOption<>( "CASUAL_NETWORK_OUTBOUND_ENABLE_LOGHANDLER" );
    public static final ConfigurationOption<Boolean> CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER = new ConfigurationOption<>( "CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER" );
    public static final ConfigurationOption<Boolean> CASUAL_NETWORK_REVERSE_INBOUND_ENABLE_LOGHANDLER = new ConfigurationOption<>( "CASUAL_NETWORK_REVERSE_INBOUND_ENABLE_LOGHANDLER" );
    public static final ConfigurationOption<String> CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL = new ConfigurationOption<>( "CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL" );
    public static final ConfigurationOption<String> CASUAL_INBOUND_NETTY_LOGGING_LEVEL = new ConfigurationOption<>( "CASUAL_INBOUND_NETTY_LOGGING_LEVEL" );
    public static final ConfigurationOption<String> CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL = new ConfigurationOption<>( "CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL" );
    public static final ConfigurationOption<UUID> CASUAL_DOMAIN_ID = new ConfigurationOption<>( "CASUAL_DOMAIN_ID" );
    public static final ConfigurationOption<String> CASUAL_DOMAIN_NAME = new ConfigurationOption<>( "CASUAL_DOMAIN_NAME" );
    public static final ConfigurationOption<String> CASUAL_CONFIG_FILE = new ConfigurationOption<>( "CASUAL_CONFIG_FILE" );
    public static final ConfigurationOption<Mode> CASUAL_INBOUND_STARTUP_MODE = new ConfigurationOption<>( "CASUAL_INBOUND_STARTUP_MODE" );
    public static final ConfigurationOption<Boolean> CASUAL_OUTBOUND_USE_EPOLL = new ConfigurationOption<>( "CASUAL_OUTBOUND_USE_EPOLL" );
    public static final ConfigurationOption<Boolean> CASUAL_INBOUND_USE_EPOLL = new ConfigurationOption<>( "CASUAL_INBOUND_USE_EPOLL" );
    public static final ConfigurationOption<Boolean> CASUAL_USE_EPOLL = new ConfigurationOption<>( "CASUAL_USE_EPOLL" );
    public static final ConfigurationOption<Long> CASUAL_INBOUND_STARTUP_INITIAL_DELAY_SECONDS = new ConfigurationOption<>( "CASUAL_INBOUND_INITIAL_DELAY_SECONDS" );
    public static final ConfigurationOption<Long> CASUAL_EVENT_SERVER_SHUTDOWN_QUIET_PERIOD_MILLIS = new ConfigurationOption<>( "CASUAL_EVENT_SERVER_SHUTDOWN_QUIET_PERIOD_SECONDS" );
    public static final ConfigurationOption<Long> CASUAL_EVENT_SERVER_SHUTDOWN_TIMEOUT_MILLIS = new ConfigurationOption<>( "CASUAL_EVENT_SERVER_SHUTDOWN_TIMEOUT_SECONDS" );
    public static final ConfigurationOption<List<String>> CASUAL_INBOUND_STARTUP_SERVICES = new ConfigurationOption<>( "CASUAL_INBOUND_STARTUP_SERVICES" );

    //TODO Why are these two now on the root?
    public static final ConfigurationOption<Integer> CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE = new ConfigurationOption<>( "CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE" );
    public static final ConfigurationOption<Boolean> CASUAL_UNMANAGED = new ConfigurationOption<>( "CASUAL_UNMANAGED" );
    //TODO CASUAL_OUTBOUND_UNMANAGED Added to allow for deprecated property until i determine what to do with it.
    public static final ConfigurationOption<Boolean> CASUAL_OUTBOUND_UNMANAGED = new ConfigurationOption<>( "CASUAL_OUTBOUND_UNMANAGED" );


    //TODO Why can't we set the inbound port here too? I know there is the JCA way of doing it with ra.xml or something but still, could we somehow add this?

    public static final ConfigurationOption<List<ReverseInbound>> CASUAL_REVERSE_INBOUND_INSTANCES = new ConfigurationOption<>( "CASUAL_REVERSE_INBOUND_INSTANCES" );

    //TODO New Environment Variables to add to configuration.md
    public static final ConfigurationOption<String> CASUAL_OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME = new ConfigurationOption<>( "CASUAL_OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME" );
    public static final ConfigurationOption<Integer> CASUAL_OUTBOUND_MANAGED_EXECUTOR_NUMBER_OF_THREADS = new ConfigurationOption<>( "CASUAL_OUTBOUND_MANAGED_EXECUTOR_NUMBER_OF_THREADS" );

    public static final ConfigurationOption<Boolean> CASUAL_EVENT_SERVER_ENABLED = new ConfigurationOption<>( "CASUAL_EVENT_SERVER_ENABLED" );
    public static final ConfigurationOption<Integer> CASUAL_EVENT_SERVER_PORT = new ConfigurationOption<>( "CASUAL_EVENT_SERVER_PORT" );
    public static final ConfigurationOption<Boolean> CASUAL_EVENT_SERVER_USE_EPOLL = new ConfigurationOption<>( "CASUAL_EVENT_SERVER_USE_EPOLL" );
    public static final ConfigurationOption<Boolean> CASUAL_EVENT_SERVER_ENABLE_LOGHANDLER = new ConfigurationOption<>( "CASUAL_EVENT_SERVER_ENABLE_LOGHANDLER" );

    //Naming?
    public static final ConfigurationOption<Integer> CASUAL_REVERSE_INBOUND_INSTANCE_CONNECTION_POOL_SIZE = new ConfigurationOption<>( "CASUAL_REVERSE_INBOUND_INSTANCE_CONNECTION_POOL_SIZE" );
    public static final ConfigurationOption<Long> CASUAL_REVERSE_INBOUND_INSTANCE_CONNECTION_MAX_BACKOFF_MILLIS = new ConfigurationOption<>( "CASUAL_REVERSE_INBOUND_INSTANCE_CONNECTION_MAX_BACKOFF_MILLIS" );

}
