/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Store for all configuration options.
 */
public class ConfigurationStore
{
    private final Map<ConfigurationOption<?>, Object> data;

    public ConfigurationStore()
    {
        data = new HashMap<>();
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
