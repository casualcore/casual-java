/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;


import se.laz.casual.network.messages.domain.TransactionType;

import java.util.Objects;

/**
 * Created by aleph on 2017-03-07.
 */
public final class Service
{
    private String name;
    private String category;
    private TransactionType transactionType;
    // microseconds
    private long timeout;
    private long hops;
    private Service()
    {}

    public static Service of(final String name, final String category, final TransactionType t)
    {
        return new Service()
            .setName(name)
            .setCategory(category)
            .setTransactionType(t);
    }



    public Service setName(String name)
    {
        this.name = name;
        return this;
    }

    public Service setCategory(String category)
    {
        this.category = category;
        return this;
    }

    public Service setTransactionType(TransactionType transactionType)
    {
        this.transactionType = transactionType;
        return this;
    }

    /**
     * Value in microseconds
     * @param timeout
     * @return
     */
    public Service setTimeout(long timeout)
    {
        this.timeout = timeout;
        return this;
    }

    public Service setHops(long hops)
    {
        this.hops = hops;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public String getCategory()
    {
        return category;
    }

    public TransactionType getTransactionType()
    {
        return transactionType;
    }

    /**
     * Value in microseconds
     * @return
     */
    public long getTimeout()
    {
        return timeout;
    }

    public long getHops()
    {
        return hops;
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
        Service service = (Service) o;
        return timeout == service.timeout &&
            hops == service.hops &&
            Objects.equals(name, service.name) &&
            Objects.equals(category, service.category) &&
            transactionType == service.transactionType;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, category, transactionType, timeout, hops);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("Service{");
        sb.append("name='").append(name).append('\'');
        sb.append(", category='").append(category).append('\'');
        sb.append(", transactionType=").append(transactionType);
        sb.append(", timeout=").append(timeout);
        sb.append(", hops=").append(hops);
        sb.append('}');
        return sb.toString();
    }
}