/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import java.util.Objects;

/**
 * Configuration option for use as a Key in configuration store map.
 *
 * @param <T> the type of data of the value of the configuration option.
 */
public class ConfigurationOption<T>
{
    private final String name;

    public ConfigurationOption( String name )
    {
        Objects.requireNonNull( name, "Value is null." );
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        ConfigurationOption<?> that = (ConfigurationOption<?>) o;
        return Objects.equals( name, that.name );
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode( name );
    }

    @Override
    public String toString()
    {
        return "ConfigurationOption{" +
                "name='" + name + '\'' +
                '}';
    }
}
