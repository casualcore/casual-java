/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import java.util.Optional;

/**
 * Read system environment variables to populate the configuration store appropriately.
 * If the environment variable is not present or the value provided is "blank", the
 * existing store values will not be updated.
 */
public class ConfigurationEnvsReader
{
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
        getEnvAsString( option.getName() ).ifPresent( value -> store.put( option, value ) );
    }

    private static void storeBooleanIfPresent( ConfigurationStore store, ConfigurationOption<Boolean> option )
    {
        getEnvAsBoolean( option.getName() ).ifPresent( value -> store.put( option, value ) );
    }

    private static void storeIntegerIfPresent( ConfigurationStore store, ConfigurationOption<Integer> option )
    {
        getEnvAsInteger( option.getName() ).ifPresent( value -> store.put( option, value ) );
    }

    private static void storeLongIfPresent( ConfigurationStore store, ConfigurationOption<Long> option )
    {
        getEnvAsLong( option.getName() ).ifPresent( value -> store.put( option, value ) );
    }

    private static Optional<String> getEnvAsString( String name )
    {
        return Optional.ofNullable( System.getenv( name ) ).map( env -> env.isBlank() ? null : env );
    }

    private static Optional<Integer> getEnvAsInteger( String name )
    {
        return getEnvAsString( name ).map( Integer::parseInt );
    }

    private static Optional<Long> getEnvAsLong( String name )
    {
        return getEnvAsString( name ).map( Long::parseLong );
    }

    private static Optional<Boolean> getEnvAsBoolean( String name )
    {
        return getEnvAsString( name ).map( Boolean::parseBoolean );
    }
}
