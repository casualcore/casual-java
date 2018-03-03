/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.service;

import se.laz.casual.network.messages.domain.TransactionType;

import java.io.Serializable;
import java.util.Objects;

public final class ServiceInfo implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final String serviceName;
    private final String category;
    private final TransactionType transactionType;

    private ServiceInfo(final String serviceName, final String category, final TransactionType transactionType )
    {
        this.serviceName = serviceName;
        this.category = category;
        this.transactionType = transactionType;
    }

    public static ServiceInfo of(final String serviceName )
    {
        return of( serviceName, "", TransactionType.AUTOMATIC );
    }

    public static ServiceInfo of(final String serviceName, final String category, final TransactionType transactionType )
    {
        Objects.requireNonNull(serviceName, "serviceName can not be null");
        Objects.requireNonNull(category, "Category can not be null" );
        Objects.requireNonNull(transactionType, "TransactionType can not be null" );
        return new ServiceInfo(serviceName, category, transactionType );
    }
    public String getServiceName()
    {
        return serviceName;
    }

    public String getCategory()
    {
        return category;
    }

    public TransactionType getTransactionType()
    {
        return transactionType;
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
        ServiceInfo that = (ServiceInfo) o;
        return Objects.equals(serviceName, that.serviceName) &&
                Objects.equals(category, that.category) &&
                transactionType == that.transactionType;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(serviceName, category, transactionType);
    }

    @Override
    public String toString()
    {
        return "ServiceInfo{" +
                "serviceName='" + serviceName + '\'' +
                ", category='" + category + '\'' +
                ", transactionType=" + transactionType +
                '}';
    }
}
