/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler;

import se.laz.casual.api.buffer.CasualBuffer;

import java.io.Serializable;
import java.util.Objects;

/**
 * Wrapper class for Inbound Request including the name of the service to be called
 * along with the buffer that should be sent to the service.
 */
public class InboundRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String serviceName;
    private final CasualBuffer buffer;

    private InboundRequest(String serviceName, CasualBuffer buffer )
    {
        this.serviceName = serviceName;
        this.buffer = buffer;
    }

    public static InboundRequest of( String serviceName, CasualBuffer buffer )
    {
        Objects.requireNonNull( serviceName, "Service name is null." );
        Objects.requireNonNull( buffer, "Buffer is null." );
        return new InboundRequest( serviceName, buffer );
    }

    public String getServiceName()
    {
        return this.serviceName;
    }

    public CasualBuffer getBuffer()
    {
        return this.buffer;
    }

    @Override
    public String toString()
    {
        return "InboundRequest{" +
                "serviceName='" + serviceName + '\'' +
                ", buffer=" + buffer +
                '}';
    }
}
