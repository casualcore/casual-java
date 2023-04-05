/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound;

import jakarta.resource.spi.XATerminator;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import jakarta.resource.spi.work.WorkManager;
import java.util.Objects;

public final class ConnectionInformation
{
    public static final String USE_LOG_HANDLER_ENV_NAME = "CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER";
    private final int port;
    private final MessageEndpointFactory factory;
    private final XATerminator xaTerminator;
    private final WorkManager workManager;
    private final boolean logHandlerEnabled;
    private final boolean useEpoll;

    private ConnectionInformation( Builder builder )
    {
        this.port = builder.port;
        this.factory = builder.factory;
        this.xaTerminator = builder.xaTerminator;
        this.workManager = builder.workManager;
        this.logHandlerEnabled = builder.logHandlerEnabled;
        this.useEpoll = builder.useEpoll;
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

    public boolean isLogHandlerEnabled()
    {
        return logHandlerEnabled;
    }

    public boolean isUseEpoll()
    {
        return useEpoll;
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
        private boolean useEpoll;
        private Boolean logHandlerEnabled;

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

        public Builder withUseEpoll( boolean useEpoll )
        {
            this.useEpoll = useEpoll;
            return this;
        }

        public Builder withEnabledLogHandler( boolean enabled )
        {
            this.logHandlerEnabled = enabled;
            return this;
        }

        public ConnectionInformation build()
        {
            Objects.requireNonNull(factory, "factory can not be null");
            Objects.requireNonNull(xaTerminator, "xaTerminator can not be null");
            Objects.requireNonNull(workManager, "workManager can not be null");
            logHandlerEnabled = Boolean.parseBoolean(System.getenv(USE_LOG_HANDLER_ENV_NAME));
            return new ConnectionInformation( this );
        }
    }
}
