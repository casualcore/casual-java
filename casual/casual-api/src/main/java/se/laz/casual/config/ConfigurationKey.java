/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import java.util.Objects;

public class ConfigurationKey<T>
{
    private final String value;

    public ConfigurationKey(String value )
    {
        Objects.requireNonNull( value, "Value is null." );
        this.value = value;
    }

    public String getValue()
    {
        return value;
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
        ConfigurationKey<?> that = (ConfigurationKey<?>) o;
        return Objects.equals( value, that.value );
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode( value );
    }

    @Override
    public String toString()
    {
        return "ConfigurationKey{" +
                "value='" + value + '\'' +
                '}';
    }
}
