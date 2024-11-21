/*
 * Copyright (c) 2021 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.config.json;

import java.util.Objects;

final class Outbound
{
    private final String managedExecutorServiceName;
    private final Integer numberOfThreads;
    private final Boolean unmanaged;
    private final Boolean useEpoll;

    private Outbound( Builder builder )
    {
        this.managedExecutorServiceName = builder.managedExecutorServiceName;
        this.numberOfThreads = builder.numberOfThreads;
        this.unmanaged = builder.unmanaged;
        this.useEpoll = builder.useEpoll;
    }

    public String getManagedExecutorServiceName()
    {
        return managedExecutorServiceName;
    }

    public Integer getNumberOfThreads()
    {
        return numberOfThreads;
    }

    public Boolean getUnmanaged()
    {
        return unmanaged;
    }

    public Boolean getUseEpoll()
    {
        return useEpoll;
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
        Outbound outbound = (Outbound) o;
        return Objects.equals( managedExecutorServiceName, outbound.managedExecutorServiceName ) && Objects.equals( numberOfThreads, outbound.numberOfThreads ) && Objects.equals( unmanaged, outbound.unmanaged ) && Objects.equals( useEpoll, outbound.useEpoll );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( managedExecutorServiceName, numberOfThreads, unmanaged, useEpoll );
    }

    @Override
    public String toString()
    {
        return "Outbound{" +
                "managedExecutorServiceName='" + managedExecutorServiceName + '\'' +
                ", numberOfThreads=" + numberOfThreads +
                ", unmanaged=" + unmanaged +
                ", useEpoll=" + useEpoll +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static Builder newBuilder( Outbound src )
    {
        return new Builder().withUnmanaged( src.getUnmanaged() )
                .withManagedExecutorServiceName( src.getManagedExecutorServiceName() )
                .withNumberOfThreads( src.getNumberOfThreads() )
                .withUseEpoll( src.getUseEpoll() );
    }

    public static final class Builder
    {
        private String managedExecutorServiceName;
        private Integer numberOfThreads;
        private Boolean unmanaged;
        private Boolean useEpoll;

        public Builder withManagedExecutorServiceName( String managedExecutorServiceName )
        {
            this.managedExecutorServiceName = managedExecutorServiceName;
            return this;
        }

        public Builder withNumberOfThreads( Integer numberOfThreads )
        {
            this.numberOfThreads = numberOfThreads;
            return this;
        }

        public Builder withUnmanaged( Boolean unmanaged )
        {
            this.unmanaged = unmanaged;
            return this;
        }

        public Builder withUseEpoll( Boolean useEpoll )
        {
            this.useEpoll = useEpoll;
            return this;
        }

        public Outbound build()
        {
            return new Outbound( this );
        }
    }
}
