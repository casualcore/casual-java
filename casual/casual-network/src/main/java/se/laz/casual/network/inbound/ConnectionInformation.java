/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound;

import javax.resource.spi.XATerminator;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
import java.util.Objects;

public final class ConnectionInformation
{
    private final int port;
    private final MessageEndpointFactory factory;
    private final XATerminator xaTerminator;
    private final WorkManager workManager;

    private ConnectionInformation(int port, MessageEndpointFactory factory, XATerminator xaTerminator, WorkManager workManager)
    {
        this.port = port;
        this.factory = factory;
        this.xaTerminator = xaTerminator;
        this.workManager = workManager;
    }

    public int getPort()
    {
        return port;
    }

    public MessageEndpointFactory getFactory()
    {
        return factory;
    }

    public XATerminator getXaTerminator()
    {
        return xaTerminator;
    }

    public WorkManager getWorkManager()
    {
        return workManager;
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private int port;
        private MessageEndpointFactory factory;
        private XATerminator xaTerminator;
        private WorkManager workManager;

        public Builder withPort(int port)
        {
            this.port = port;
            return this;
        }

        public Builder withFactory(MessageEndpointFactory factory)
        {
            this.factory = factory;
            return this;
        }

        public Builder withXaTerminator(XATerminator xaTerminator)
        {
            this.xaTerminator = xaTerminator;
            return this;
        }

        public Builder withWorkManager(WorkManager workManager)
        {
            this.workManager = workManager;
            return this;
        }

        public ConnectionInformation build()
        {
            Objects.requireNonNull(factory, "factory can not be null");
            Objects.requireNonNull(xaTerminator, "xaTerminator can not be null");
            Objects.requireNonNull(workManager, "workManager can not be null");
            return new ConnectionInformation(port, factory, xaTerminator, workManager);
        }
    }
}
