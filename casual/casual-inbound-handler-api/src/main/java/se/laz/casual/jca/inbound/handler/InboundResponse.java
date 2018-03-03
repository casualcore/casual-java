/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler;

import se.laz.casual.api.buffer.CasualBuffer;

import java.io.Serializable;
import java.util.Objects;

public class InboundResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final boolean successful;
    private final CasualBuffer buffer;

    private InboundResponse( boolean successful, CasualBuffer buffer)
    {
        this.successful = successful;
        this.buffer = buffer;
    }

    public static InboundResponse of( boolean successful, CasualBuffer buffer )
    {
        Objects.requireNonNull( buffer, "Buffer is null." );
        return new InboundResponse( successful, buffer );
    }

    public boolean isSuccessful()
    {
        return this.successful;
    }

    public CasualBuffer getBuffer( )
    {
        return this.buffer;
    }

    @Override
    public String toString()
    {
        return "InboundResponse{" +
                "successful=" + successful +
                ", buffer=" + buffer +
                '}';
    }
}
