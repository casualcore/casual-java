/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.json.provider;

import java.util.Objects;

public class WithoutDefaultCtor
{
    private final String value;

    public WithoutDefaultCtor( String value )
    {
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
        WithoutDefaultCtor that = (WithoutDefaultCtor) o;
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
        return "WithoutDefaultCtor{" +
                "value='" + value + '\'' +
                '}';
    }
}
