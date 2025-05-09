/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound;

import se.laz.casual.api.CasualRuntimeException;
import se.laz.casual.network.ProtocolVersion;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ValueHolder implements Supplier<ProtocolVersion>, Consumer<ProtocolVersion>
{
    private ProtocolVersion protocolVersion;

    private ValueHolder()
    {}

    public static ValueHolder of()
    {
        return new ValueHolder();
    }

    @Override
    public ProtocolVersion get()
    {
        if(null == protocolVersion)
        {
            throw new CasualRuntimeException("protocol version not set - should never be used in a context where it has not already been set");
        }
        return protocolVersion;
    }

    @Override
    public void accept(ProtocolVersion protocolVersion)
    {
        Objects.requireNonNull(protocolVersion, "protocolVersion can not be null");
        this.protocolVersion = protocolVersion;
    }
}
