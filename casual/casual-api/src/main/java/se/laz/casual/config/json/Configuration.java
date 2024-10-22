/*
 * Copyright (c) 2021 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Configuration
{
    private final Inbound inbound;
    private final Domain domain;
    private final Outbound outbound;
    private final List<ReverseInbound> reverseInbound;
    private final EventServer eventServer;
    private final Boolean useEpoll;
    private final Boolean unmanaged;

    public Configuration( Builder builder )
    {
        this.inbound = builder.inbound;
        this.domain = builder.domain;
        this.outbound = builder.outbound;
        this.reverseInbound = builder.reverseInbound;
        this.eventServer = builder.eventServer;
        this.useEpoll = builder.useEpoll;
        this.unmanaged = builder.unmanaged;
    }

    public Inbound getInbound()
    {
        return inbound;
    }

    public Domain getDomain()
    {
        return domain;
    }

    public Outbound getOutbound()
    {
        return outbound;
    }

    public List<ReverseInbound> getReverseInbound()
    {
        return reverseInbound;
    }

    public EventServer getEventServer()
    {
        return eventServer;
    }

    public Boolean getUseEpoll()
    {
        return useEpoll;
    }

    public Boolean getUnmanaged()
    {
        return unmanaged;
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        Configuration that = (Configuration) o;
        return Objects.equals( inbound, that.inbound ) && Objects.equals( domain, that.domain ) && Objects.equals( outbound, that.outbound ) && Objects.equals( reverseInbound, that.reverseInbound ) && Objects.equals( eventServer, that.eventServer ) && Objects.equals( useEpoll, that.useEpoll ) && Objects.equals( unmanaged, that.unmanaged );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( inbound, domain, outbound, reverseInbound, eventServer, useEpoll, unmanaged );
    }

    @Override
    public String toString()
    {
        return "Configuration{" +
                "inbound=" + inbound +
                ", domain=" + domain +
                ", outbound=" + outbound +
                ", reverseInbound=" + reverseInbound +
                ", eventServer=" + eventServer +
                ", useEpoll=" + useEpoll +
                ", unmanaged=" + unmanaged +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static Builder newBuilder( Configuration src )
    {
        return new Builder().withInbound( src.getInbound() )
                .withOutbound( src.getOutbound() )
                .withReverseInbound( src.getReverseInbound() )
                .withEventServer( src.getEventServer() )
                .withUseEpoll( src.getUseEpoll() )
                .withUnmanaged( src.getUnmanaged() )
                .withDomain( src.getDomain() );
    }

    public static final class Builder
    {
        private Inbound inbound;
        private Domain domain;
        private Outbound outbound;
        private List<ReverseInbound> reverseInbound = new ArrayList<>();
        private EventServer eventServer;
        private Boolean useEpoll;
        private Boolean unmanaged;

        public Builder withInbound( Inbound inbound )
        {
            this.inbound = inbound;
            return this;
        }

        public Builder withDomain( Domain domain )
        {
            this.domain = domain;
            return this;
        }

        public Builder withOutbound( Outbound outbound )
        {
            this.outbound = outbound;
            return this;
        }

        public Builder withReverseInbound( List<ReverseInbound> reverseInbounds )
        {
            this.reverseInbound = reverseInbounds;
            return this;
        }

        public Builder withEventServer( EventServer eventServer )
        {
            this.eventServer = eventServer;
            return this;
        }

        public Builder withUseEpoll( Boolean useEpoll )
        {
            this.useEpoll = useEpoll;
            return this;
        }

        public Builder withUnmanaged( Boolean unmanaged )
        {
            this.unmanaged = unmanaged;
            return this;
        }

        public Configuration build()
        {
            return new Configuration( this );
        }
    }
}
