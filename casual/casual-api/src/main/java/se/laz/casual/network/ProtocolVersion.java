/*
 * Copyright (c) 2022 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network;

import se.laz.casual.network.connection.CasualConnectionException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ProtocolVersion
{
    VERSION_1_0(1000, "1.0", true),
    VERSION_1_1(1001, "1.1", true),
    VERSION_1_2(1002, "1.2", true);

    private static final List<Long> supportedVersions;
    private static final List<String> supportedVersionsString;
    private static final Map<Long, ProtocolVersion> longVersions;
    private static final Map<String,ProtocolVersion> stringVersions;

    static
    {
        supportedVersions = createSupportedList( p-> p.version );
        supportedVersionsString = createSupportedList( p-> p.versionString );

        longVersions = createMarshallingMap( p-> p.version );
        stringVersions = createMarshallingMap( p->p.versionString );
    }

    static <T>List<T> createSupportedList( Function<ProtocolVersion,T> function )
    {
        return Arrays.stream( ProtocolVersion.values() )
                .filter( p -> p.supported )
                .map( function )
                .collect( Collectors.toList());
    }

    static <T>Map<T,ProtocolVersion> createMarshallingMap( Function<ProtocolVersion,T> function )
    {
        Map<T,ProtocolVersion> map = new HashMap<>();
        for( ProtocolVersion v : ProtocolVersion.values() )
        {
            map.put( function.apply( v ), v );
        }
        return map;
    }

    private final long version;
    private final String versionString;
    private final boolean supported;

    ProtocolVersion(long version, String versionString, boolean supported)
    {
        this.version = version;
        this.versionString = versionString;
        this.supported = supported;
    }

    public long getVersion()
    {
        return version;
    }

    public String getVersionAsString()
    {
        return versionString;
    }

    public boolean isSupported()
    {
        return supported;
    }

    public static ProtocolVersion unmarshall(long version)
    {
        return Optional.ofNullable( longVersions.get( version ) )
                     .orElseThrow(() -> new CasualConnectionException("Version: " + version + " is not supported"));
    }

    public static ProtocolVersion unmarshall(String version)
    {
        return Optional.ofNullable( stringVersions.get( version ) )
                .orElseThrow(() -> new CasualConnectionException("Unknown protocol version: " + version) );
    }

    public static List<Long> supportedVersionNumbers()
    {
        return supportedVersions;
    }

    public static List<String> supportedVersions()
    {
        return supportedVersionsString;
    }

}
