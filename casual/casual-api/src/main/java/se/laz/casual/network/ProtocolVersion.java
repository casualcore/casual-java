/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network;

import se.laz.casual.network.connection.CasualConnectionException;

import java.util.Arrays;
import java.util.List;

public enum ProtocolVersion
{
    VERSION_1_0(1000),
    VERSION_1_1(1001),
    VERSION_1_2(1002);

    private static final List<Long> supportedVersions = Arrays.asList(ProtocolVersion.VERSION_1_0.getVersion(), ProtocolVersion.VERSION_1_1.getVersion());
    private long version;

    ProtocolVersion(long version)
    {
        this.version = version;
    }

    public long getVersion()
    {
        return version;
    }

    public String getVersionAsString()
    {
        if(version == ProtocolVersion.VERSION_1_0.getVersion())
        {
            return "1.0";
        }
        if(version == ProtocolVersion.VERSION_1_1.getVersion())
        {
            return "1.1";
        }
        if(version == ProtocolVersion.VERSION_1_2.getVersion())
        {
            return "1.2";
        }
        throw new CasualConnectionException("Unknown protocol version: " + version);
    }

    public static ProtocolVersion unmarshall(long version)
    {
        return Arrays.stream(values())
                     .filter(protocolVersion -> protocolVersion.getVersion() == version)
                     .findFirst()
                     .orElseThrow(() -> new CasualConnectionException("Version: " + version + " is not supported"));
    }

    public static ProtocolVersion unmarshall(String version)
    {
        switch(version)
        {
            case "1.0":
                return VERSION_1_0;
            case "1.1":
                return VERSION_1_1;
            case "1.2":
                return VERSION_1_2;
            default:
                throw new CasualConnectionException("Unknown protocol version: " + version);
        }
    }

    public static List<Long> supportedVersions()
    {
        return supportedVersions;
    }

}
