/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network;

import se.laz.casual.network.connection.CasualConnectionException;

import java.util.Arrays;

public enum ProtocolVersion
{
    VERSION_1_0(1000),
    VERSION_1_1(1001),
    VERSION_1_2(1002);

    private long version;

    ProtocolVersion(long version)
    {
        this.version = version;
    }

    public long getVersion()
    {
        return version;
    }

    public static ProtocolVersion unmarshall(long version)
    {
        return Arrays.stream(values())
                     .filter(protocolVersion -> protocolVersion.getVersion() == version)
                     .findFirst()
                     .orElseThrow(() -> new CasualConnectionException("Version: " + version + " is not supported"));
    }

}
