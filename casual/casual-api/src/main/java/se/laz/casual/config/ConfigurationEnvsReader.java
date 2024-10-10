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
    private static final Logger logger = Logger.getLogger( ConfigurationEnvsReader.class.getName() );

    private final ConfigurationStore store;

    public ConfigurationEnvsReader( ConfigurationStore store )
    {
        this.store = store;
    }

    /**
     * Populate configuration store with value for a configuration file if the environment variable is set.
     */
    public void populateConfigFileEnv()
    {
        storeStringIfPresent( ConfigurationOptions.CASUAL_CONFIG_FILE );
    }

    /**
     * Populate configuration store with all known values present in the environment variables.
     */
    public void populateStoreFromEnvs()
    {
        populateFieldedEnvs();
        populateLogHandlers();
        populateNettyLogLevels();
        populateInbound();
        populateEpoll();
        populateEventServer();
        populateUnmanaged();
        populateOutbound();
    }

    private void populateOutbound()
    {
        storeIntegerIfPresent( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_NUMBER_OF_THREADS );
        storeStringIfPresent( ConfigurationOptions.CASUAL_OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME );
    }

    private void populateUnmanaged()
    {
        storeIntegerIfPresent( ConfigurationOptions.CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE );
        storeBooleanIfPresent( ConfigurationOptions.CASUAL_UNMANAGED );
    }

    private void populateEventServer()
    {
        storeIntegerIfPresent( ConfigurationOptions.CASUAL_EVENT_SERVER_PORT );
        storeBooleanIfPresent( ConfigurationOptions.CASUAL_EVENT_SERVER_USE_EPOLL );
        populateEventServerShutdown();
    }

    private void populateEventServerShutdown()
    {
        storeLongIfPresent( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_TIMEOUT_MILLIS );
        storeLongIfPresent( ConfigurationOptions.CASUAL_EVENT_SERVER_SHUTDOWN_QUIET_PERIOD_MILLIS );
    }

    private void populateEpoll()
    {
        storeBooleanIfPresent( ConfigurationOptions.CASUAL_USE_EPOLL );
        storeBooleanIfPresent( ConfigurationOptions.CASUAL_INBOUND_USE_EPOLL );
        storeBooleanIfPresent( ConfigurationOptions.CASUAL_OUTBOUND_USE_EPOLL );
    }

    private void populateInbound()
    {
        populateInboundStartupMode();
        storeLongIfPresent( ConfigurationOptions.CASUAL_INBOUND_STARTUP_INITIAL_DELAY_SECONDS );
    }

    private void populateInboundStartupMode()
    {
        storeModeIfPresent( ConfigurationOptions.CASUAL_INBOUND_STARTUP_MODE );
    }

    private void populateNettyLogLevels()
    {
        storeStringIfPresent( ConfigurationOptions.CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL );
        storeStringIfPresent( ConfigurationOptions.CASUAL_INBOUND_NETTY_LOGGING_LEVEL );
        storeStringIfPresent( ConfigurationOptions.CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL );
    }

    private void populateLogHandlers()
    {
        storeBooleanIfPresent( ConfigurationOptions.CASUAL_NETWORK_OUTBOUND_ENABLE_LOGHANDLER );
        storeBooleanIfPresent( ConfigurationOptions.CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER );
        storeBooleanIfPresent( ConfigurationOptions.CASUAL_NETWORK_REVERSE_INBOUND_ENABLE_LOGHANDLER );
    }

    private void populateFieldedEnvs()
    {
        storeStringIfPresent( ConfigurationOptions.CASUAL_API_FIELDED_ENCODING );
        storeStringIfPresent( ConfigurationOptions.CASUAL_FIELD_TABLE );
    }

    private void storeStringIfPresent( ConfigurationOption<String> option )
    {
        storeIfPresent( option, this::getEnvAsString );
    }

    private void storeBooleanIfPresent( ConfigurationOption<Boolean> option )
    {
        storeIfPresent( option, this::getEnvAsBoolean );
    }

    private void storeIntegerIfPresent( ConfigurationOption<Integer> option )
    {
        storeIfPresent( option, this::getEnvAsInteger );
    }

    private void storeLongIfPresent( ConfigurationOption<Long> option )
    {
        storeIfPresent( option, this::getEnvAsLong );
    }

    private void storeModeIfPresent( ConfigurationOption<Mode> option )
    {
        storeIfPresent( option, this::getEnvAsMode );
    }

    private <T> void storeIfPresent( ConfigurationOption<T> option, Function<String, Optional<T>> getEnvFunction )
    {
        getEnvFunction.apply( option.getName() ).ifPresent( value -> store.put( option, value ) );
    }

    private Optional<String> getEnvAsString( String name )
    {
        return Optional.ofNullable( System.getenv( name ) ).map( env -> env.isBlank() ? null : env );
    }

    private Optional<Integer> getEnvAsInteger( String name )
    {
        return getEnvAsString( name ).map( v -> castOrThrow( name, v, Integer::parseInt ) );
    }

    private Optional<Long> getEnvAsLong( String name )
    {
        return getEnvAsString( name ).map( v -> castOrThrow( name, v, Long::parseLong ) );
    }

    private Optional<Boolean> getEnvAsBoolean( String name )
    {
        return getEnvAsString( name ).map( v -> castOrThrow( name, v, Boolean::parseBoolean ) );
    }

    private Optional<Mode> getEnvAsMode( String name )
    {
        return getEnvAsString( name ).map( v -> castOrThrow( name, v, Mode::fromName ) );
    }

    private <T> T castOrThrow( String name, String value, Function<String, T> castFunction )
    {
        try
        {
            return castFunction.apply( value );
        }
        catch( IllegalArgumentException e )
        {
            Supplier<String> message =
                    () -> "Invalid environment variable data: " + name + " has value: '" + value + "'.";
            logger.severe( message );
            throw new ConfigurationException( message.get(), e );
        }
    }
}
