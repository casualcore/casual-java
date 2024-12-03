/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.json.provider;

import java.util.Objects;

public class MultipleCtorWithoutDefault
{
    private final String value;
    private final int other;

    public MultipleCtorWithoutDefault( String value, int other )
    {
        this.value = value;
        this.other = other;
    }

    public MultipleCtorWithoutDefault( String value )
    {
        this( value, 123 );
    }

    public String getValue()
    {
        return value;
    }

    public int getOther()
    {
        return other;
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
        MultipleCtorWithoutDefault that = (MultipleCtorWithoutDefault) o;
        return other == that.other && Objects.equals( value, that.value );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( value, other );
    }

    @Override
    public String toString()
    {
        return "MultipleCtorWithoutDefault{" +
                "value='" + value + '\'' +
                ", other=" + other +
                '}';
    }
}
