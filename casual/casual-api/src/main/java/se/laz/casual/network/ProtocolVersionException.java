/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network;

import se.laz.casual.api.CasualRuntimeException;

import java.util.function.Supplier;

public class ProtocolVersionException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;
    public ProtocolVersionException(Supplier<String> message)
    {
        super(message.get());
    }
}
