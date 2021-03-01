/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public enum Mode
{
    @SerializedName(Constants.IMMEDIATE)
    IMMEDIATE( Constants.IMMEDIATE ),
    @SerializedName(Constants.TRIGGER)
    TRIGGER( Constants.TRIGGER ),
    @SerializedName(Constants.DISCOVER)
    DISCOVER( Constants.DISCOVER );

    private static Map<String,Mode> lookup = new HashMap<>(  );

    static
    {
        for( Mode m: Mode.values() )
        {
            lookup.put( m.getName(), m );
        }
    }

    private String name;

    Mode( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public static Mode fromName( String name )
    {
        if( !lookup.containsKey( name ) )
        {
            throw new IllegalArgumentException( "Name is not a valid Mode: " + name );
        }

        return lookup.get( name );
    }

    public static class Constants
    {
        private Constants()
        {
        }

        public static final String IMMEDIATE = "immediate";
        public static final String TRIGGER = "trigger";
        public static final String DISCOVER = "discover";

        public static final String TRIGGER_SERVICE = "casual.internal.inbound.startup.service";
    }
}
