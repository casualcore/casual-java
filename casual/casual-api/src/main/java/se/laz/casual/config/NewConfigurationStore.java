/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NewConfigurationStore
{
    private static final Map<ConfigurationKey<?>, Object> DATA = new HashMap<>();

    static
    {
        DATA.put( ConfigurationKeys.CASUAL_API_FIELDED_ENCODING, "UTF8" );
        DATA.put( ConfigurationKeys.CASUAL_INBOUND_STARTUP_MODE, Mode.TRIGGER );
        DATA.put( ConfigurationKeys.CASUAL_INBOUND_STARTUP_SERVICES, Arrays.asList( "service1", "service2") );
    }

    @SuppressWarnings( "unchecked" )
    public static <T> T get( final ConfigurationKey<T> key )
    {
        return (T) DATA.get( key );
    }

}
