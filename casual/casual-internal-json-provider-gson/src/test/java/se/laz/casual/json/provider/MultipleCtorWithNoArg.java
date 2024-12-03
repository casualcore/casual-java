/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.json.provider;

import java.util.Objects;

public class MultipleCtorWithNoArg
{
    private String value;

    public MultipleCtorWithNoArg( )
    {
        this( "defaulting" );
    }

    public MultipleCtorWithNoArg( String value )
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue( String value )
    {
        this.value = value;
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
        MultipleCtorWithNoArg that = (MultipleCtorWithNoArg) o;
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
        return "MultipleCtorWithNoArg{" +
                "value='" + value + '\'' +
                '}';
    }
}
