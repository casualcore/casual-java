/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json;

import java.util.Objects;
import java.util.UUID;

/**
 * Holds the fixed values for id and name for the whole domain.
 */
final class Domain
{
    private final UUID id;
    private final String name;

    private Domain( Builder builder )
    {
        this.id = builder.id;
        this.name = builder.name;
    }

    public UUID getId()
    {
        return id;
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
        Domain domain = (Domain) o;
        return Objects.equals( id, domain.id ) && Objects.equals( name, domain.name );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( id, name );
    }

    @Override
    public String toString()
    {
        return "Domain{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static Builder newBuilder( Domain domain )
    {
        return newBuilder().id( domain.getId() ).name( domain.getName() );
    }

    public static final class Builder
    {
        private UUID id;
        private String name;

        public Builder id( UUID id )
        {
            this.id = id;
            return this;
        }

        public Builder name( String name )
        {
            this.name = name;
            return this;
        }

        public Domain build()
        {
            return new Domain( this );
        }
    }
}
