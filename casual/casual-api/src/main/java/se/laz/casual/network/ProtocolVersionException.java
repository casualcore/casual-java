/*
 * Copyright (c) 2025 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network;

import se.laz.casual.network.connection.CasualConnectionException;

import java.util.function.Supplier;

// java:S110 - Deep inheritance ok
@SuppressWarnings( "java:S110" )
public class ProtocolVersionException extends CasualConnectionException
{
    private static final long serialVersionUID = 1L;
    public ProtocolVersionException(Supplier<String> message)
    {
        super(message.get());
    }
}
