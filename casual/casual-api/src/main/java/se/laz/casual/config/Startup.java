/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class Startup
{
    private static final Logger LOG = Logger.getLogger(Startup.class.getName());
    private final Mode mode;
    private final List<String> services;

    public Startup( Mode mode, List<String> services )
    {
        this.mode = mode;
        this.services = services;
    }

    public Mode getMode()
    {
        return mode;
    }

    public List<String> getServices()
    {
        switch( this.mode )
        {
            case IMMEDIATE:
                return Collections.emptyList();
            case TRIGGER:
                return Collections.singletonList( Mode.Constants.TRIGGER_SERVICE );
            default:
                return services == null ? Collections.emptyList() : services;
        }
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
        return getMode() == startup.getMode() &&
                Objects.equals( getServices(), startup.getServices() );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( getMode(), getServices() );
    }

    @Override
    public String toString()
    {
        return "Startup{" +
                "mode=" + getMode() +
                ", services=" + getServices() +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private Mode mode = Mode.IMMEDIATE;
        private List<String> services = new ArrayList<>(  );

        private Builder()
        {
        }

        public Builder withMode( Mode mode )
        {
            this.mode = mode;
            return this;
        }

        public Builder withServices( List<String> services )
        {
            this.services = services;
            return this;
        }

        public Startup build()
        {
            LOG.info(() -> "Casual Inbound Startup mode is: " + mode);
            return new Startup( mode, services );
        }
    }
}
