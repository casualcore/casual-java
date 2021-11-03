/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.service;

import se.laz.casual.network.messages.domain.TransactionType;

import java.util.Objects;

public final class ServiceDetails
{
    private final String name;
    private final String category;
    private final TransactionType transactionType;
    private final long timeout;
    private final long hops;

    private ServiceDetails(String name, String category, TransactionType transactionType, long timeout, long hops)
    {
        this.name = name;
        this.category = category;
        this.transactionType = transactionType;
        this.timeout = timeout;
        this.hops = hops;
    }

    public String getName()
    {
        return name;
    }

    public long getHops()
    {
        return hops;
    }

    public String getCategory()
    {
        return category;
    }

    public TransactionType getTransactionType()
    {
        return transactionType;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public static ServiceDetails.Builder createBuilder()
    {
        return new ServiceDetails.Builder();
    }

    @Override
    public int hashCode()
    {
        return name.hashCode() + Long.hashCode(hops);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        ServiceDetails that = (ServiceDetails) o;
        return Objects.equals(name, that.name) &&
                hops == that.hops;
    }

    @Override
    public String toString()
    {
        return "ServiceDetails{" +
                "name='" + name + '\'' +
                ", hops=" + hops +
                '}';
    }

    public static final class Builder
    {
        private String name;
        private String category;
        private TransactionType transactionType;
        private long timeout;
        private long hops;

        public ServiceDetails.Builder withName(String name)
        {
            this.name = name;
            return this;
        }

        public ServiceDetails.Builder withCategory(String category)
        {
            this.category = category;
            return this;
        }

        public ServiceDetails.Builder withTransactionType(TransactionType transactionType)
        {
            this.transactionType = transactionType;
            return this;
        }

        public ServiceDetails.Builder withTimeout(long timeout)
        {
            this.timeout = timeout;
            return this;
        }

        public ServiceDetails.Builder withHops(long hops)
        {
            this.hops = hops;
            return this;
        }

        public ServiceDetails build()
        {
            Objects.requireNonNull(name);
            Objects.requireNonNull(category);
            Objects.requireNonNull(transactionType);
            if (timeout < 0)
            {
                throw new IllegalArgumentException("Can't have negative timeout, got value: " + timeout);
            }
            if (hops < 0)
            {
                throw new IllegalArgumentException("Can't have fewer hops than 0. Services will always have 1 hop or more, got value: " + hops);
            }
            return new ServiceDetails(name, category, transactionType, timeout, hops);
        }
    }
}
