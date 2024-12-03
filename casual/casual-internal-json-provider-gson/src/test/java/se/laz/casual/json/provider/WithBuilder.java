/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.json.provider;

import java.util.Objects;

public class WithBuilder
{
    private final String value;

    private WithBuilder( Builder builder )
    {
        this.value = builder.value;
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
        WithBuilder that = (WithBuilder) o;
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
        return "WithBuilder{" +
                "value='" + value + '\'' +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static Builder newBuilder( WithBuilder src )
    {
        return new Builder().withValue( src.getValue() );
    }

    public static class Builder
    {
        private String value;

        public Builder()
        {
        }

        public Builder withValue( String value )
        {
            this.value = value;
            return this;
        }

        public WithBuilder build()
        {
            return new WithBuilder( this );
        }
    }
}
