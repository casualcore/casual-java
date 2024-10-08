/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Read system environment variables to populate the configuration store appropriately.
 * <br/>
 * NB - If the environment variable is not present or the value provided is blank ({@link String#isBlank()});
 * then the existing store value remains unchanged.
 */
public class ConfigurationEnvsReader
{
    private static final Logger logger = Logger.getLogger(ConfigurationEnvsReader.class.getName());
    /**
     * Populate configuration store with value for a configuration file if the environment variable is set.
     */
    public static void populateConfigFileEnv( ConfigurationStore store )
    {
        storeStringIfPresent( store, ConfigurationOptions.CASUAL_CONFIG_FILE );
    }

    public static void populateStoreFromEnvs( ConfigurationStore store )
    {
        populateFieldedEnvs( store );
        populateLogHandlers( store );
        populateNettyLogLevels( store );
        populateInbound( store );
        populateEpoll( store );
        populateEventServer( store );
        populateUnmanaged( store );
        populateOutbound( store );
    }

    private static void populateOutbound( ConfigurationStore store )
    {
        storeIntegerIfPresent( store, ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_NUMBER_OF_THREADS );
        storeStringIfPresent( store, ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME );
    }

    private static void populateUnmanaged( ConfigurationStore store )
    {
        storeIntegerIfPresent( store, ConfigurationOptions.CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE );
        storeBooleanIfPresent( store, ConfigurationOptions.CASUAL_UNMANAGED );
    }

    private static void populateEventServer( ConfigurationStore store )
    {
        storeIntegerIfPresent( store, ConfigurationOptions.CASUAL_EVENT_SERVER_PORT );
        storeBooleanIfPresent( store, ConfigurationOptions.CASUAL_EVENT_SERVER_USE_EPOLL );
        populateEventServerShutdown( store );
    }

    private static void populateEventServerShutdown( ConfigurationStore store )
    {
        storeIntegerIfPresent( store, ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_TIMEOUT_MILLIS );
        storeIntegerIfPresent( store, ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_QUIET_PERIOD_MILLIS );
    }

    private static void populateEpoll( ConfigurationStore store )
    {
        storeBooleanIfPresent( store, ConfigurationOptions.CASUAL_USE_EPOLL );
        storeBooleanIfPresent( store, ConfigurationOptions.CASUAL_INBOUND_USE_EPOLL );
        storeBooleanIfPresent( store, ConfigurationOptions.CASUAL_OUTBOUND_USE_EPOLL );
    }

    private static void populateInbound( ConfigurationStore store )
    {
        populateInboundStartupMode( store );
        storeLongIfPresent( store, ConfigurationOptions.CASUAL_INBOUND_STARTUP_INITIAL_DELAY_SECONDS );
    }

    private static void populateInboundStartupMode( ConfigurationStore store )
    {
        storeModeIfPresent( store, ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE );
    }

    private static void populateNettyLogLevels( ConfigurationStore store )
    {
        storeStringIfPresent( store, ConfigurationOptions.CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL );
        storeStringIfPresent( store, ConfigurationOptions.CASUAL_INBOUND_NETTY_LOGGING_LEVEL );
        storeStringIfPresent( store, ConfigurationOptions.CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL );
    }

    private static void populateLogHandlers( ConfigurationStore store )
    {
        storeBooleanIfPresent( store, ConfigurationOptions.CASUAL_NETWORK_OUTBOUND_ENABLE_LOGHANDLER );
        storeBooleanIfPresent( store, ConfigurationOptions.CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER );
        storeBooleanIfPresent( store, ConfigurationOptions.CASUAL_NETWORK_REVERSE_INBOUND_ENABLE_LOGHANDLER );
    }

    private static void populateFieldedEnvs( ConfigurationStore store )
    {
        storeStringIfPresent( store, ConfigurationOptions.CASUAL_API_FIELDED_ENCODING );
        storeStringIfPresent( store, ConfigurationOptions.CASUAL_FIELD_TABLE );
    }

    private static void storeStringIfPresent( ConfigurationStore store, ConfigurationOption<String> option )
    {
        storeIfPresent( store, option, ConfigurationEnvsReader::getEnvAsString );
    }

    private static void storeBooleanIfPresent( ConfigurationStore store, ConfigurationOption<Boolean> option )
    {
        storeIfPresent( store, option, ConfigurationEnvsReader::getEnvAsBoolean );
    }

    private static void storeIntegerIfPresent( ConfigurationStore store, ConfigurationOption<Integer> option )
    {
        storeIfPresent( store, option, ConfigurationEnvsReader::getEnvAsInteger );
    }

    private static void storeLongIfPresent( ConfigurationStore store, ConfigurationOption<Long> option )
    {
        storeIfPresent( store, option, ConfigurationEnvsReader::getEnvAsLong );
    }

    private static void storeModeIfPresent( ConfigurationStore store, ConfigurationOption<Mode> option )
    {
        storeIfPresent( store, option, ConfigurationEnvsReader::getEnvAsMode );
    }

    private static <T> void storeIfPresent( ConfigurationStore store, ConfigurationOption<T> option, Function<String,Optional<T>> getEnvFunction )
    {
        getEnvFunction.apply( option.getName() ).ifPresent( value -> store.put( option, value ) );
    }

    private static Optional<String> getEnvAsString( String name )
    {
        return Optional.ofNullable( System.getenv( name ) ).map( env -> env.isBlank() ? null : env );
    }

    private static Optional<Integer> getEnvAsInteger( String name )
    {
        return getEnvAsString( name ).map( v -> castOrThrow( name, v, Integer::parseInt ) );
    }

    private static Optional<Long> getEnvAsLong( String name )
    {
        return getEnvAsString( name ).map( v -> castOrThrow( name, v, Long::parseLong ) );
    }

    private static Optional<Boolean> getEnvAsBoolean( String name )
    {
        return getEnvAsString( name ).map( v -> castOrThrow( name, v, Boolean::parseBoolean ) );
    }

    private static Optional<Mode> getEnvAsMode( String name )
    {
        return getEnvAsString( name ).map( v -> castOrThrow( name, v, Mode::fromName ) );
    }

    private static <T> T castOrThrow( String name, String value, Function<String,T> castFunction)
    {
        try
        {
            return castFunction.apply( value );
        }
        catch( IllegalArgumentException e )
        {
            Supplier<String> message = ()-> "Invalid environment variable data: " + name + " has value: '" + value + "'.";
            logger.severe( message );
            throw new ConfigurationException( message.get(), e );
        }
    }
}
