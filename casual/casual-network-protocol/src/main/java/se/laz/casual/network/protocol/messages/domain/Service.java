/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.domain;


import se.laz.casual.network.messages.domain.TransactionType;
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;
import se.laz.casual.network.protocol.messages.parseinfo.DiscoveryReplySizes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * We assume that everything for a Service fits into Integer.MAX_VALUE
     * If not, we throw a CasualProtocolException
     * @return
     */
    public List<byte[]> toNetworkBytes()
    {
        final byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        final byte[] categoryBytes = category.getBytes(StandardCharsets.UTF_8);
        final long serviceByteSize = DiscoveryReplySizes.SERVICES_ELEMENT_NAME_SIZE.getNetworkSize() + nameBytes.length +
                                    DiscoveryReplySizes.SERVICES_ELEMENT_CATEGORY_SIZE.getNetworkSize() + categoryBytes.length +
                                    DiscoveryReplySizes.SERVICES_ELEMENT_TRANSACTION.getNetworkSize() +
                                    DiscoveryReplySizes.SERVICES_ELEMENT_TIMEOUT.getNetworkSize() +
                                   (long) DiscoveryReplySizes.SERVICES_ELEMENT_HOPS.getNetworkSize();
        if(serviceByteSize > Integer.MAX_VALUE)
        {
            throw new CasualProtocolException("Queue byte size is larger than Integer.MAX_VALUE: " + serviceByteSize);
        }

        return toNetworkBytesFitsInOneBuffer((int)serviceByteSize, nameBytes, categoryBytes);
    }


    private List<byte[]> toNetworkBytesFitsInOneBuffer(final int serviceByteSize, final byte[] nameBytes, final byte[] categoryBytes)
    {
        final List<byte[]> l = new ArrayList<>();
        final ByteBuffer b = ByteBuffer.allocate(serviceByteSize);
        b.putLong(nameBytes.length)
         .put(nameBytes)
         .putLong(categoryBytes.length)
         .put(categoryBytes)
         .putShort(TransactionType.marshal(transactionType))
         .putLong(timeout)
         .putLong(hops);
        l.add(b.array());
        return l;
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