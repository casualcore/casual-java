/*
 * Copyright (c) 2021 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class Startup
{
    private final Mode mode;
    private final List<String> services;

    private Startup( Builder builder )
    {
        this.mode = builder.mode;
        this.services = builder.services;
    }

    public Mode getMode()
    {
        return mode;
    }

    public List<String> getServices()
    {
        return services == null ? Collections.emptyList() : new ArrayList<>( services );
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
        Startup startup = (Startup) o;
        return mode == startup.mode && Objects.equals( services, startup.services );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( mode, services );
    }

    @Override
    public String toString()
    {
        return "Startup{" +
                "mode=" + mode +
                ", services=" + services +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static Builder newBuilder( Startup src)
    {
        return new Builder().withMode( src.getMode() ).withServices( src.getServices() );
    }

    public static final class Builder
    {
        private Mode mode;
        private List<String> services = new ArrayList<>();

        public Builder withMode( Mode mode )
        {
            this.mode = mode;
            return this;
        }

        public Builder withServices( List<String> services )
        {
            this.services = new ArrayList<>( services );
            return this;
        }

        public Startup build()
        {
            Collections.sort( services );

            return new Startup( this );
        }
    }
}
