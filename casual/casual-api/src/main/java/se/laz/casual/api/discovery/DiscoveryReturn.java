/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.discovery;

import se.laz.casual.api.queue.QueueDetails;
import se.laz.casual.api.service.ServiceDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
public class DiscoveryReturn
{
    private final List<ServiceDetails> serviceDetails;
    private final List<QueueDetails> queueDetails;
    private DiscoveryReturn(List<ServiceDetails> serviceDetails, List<QueueDetails> queueDetails)
    {
        this.serviceDetails = serviceDetails;
        this.queueDetails = queueDetails;
    }

    public List<ServiceDetails> getServiceDetails()
    {
        return Collections.unmodifiableList(serviceDetails);
    }

    public List<QueueDetails> getQueueDetails()
    {
        return Collections.unmodifiableList(queueDetails);
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private List<QueueDetails> queueDetails = new ArrayList<>();
        private List<ServiceDetails> serviceDetails = new ArrayList<>();

        private Builder()
        {}

        public Builder addQueueDetails(QueueDetails queueDetails)
        {
            Objects.requireNonNull(queueDetails, "queueDetails can not be null");
            this.queueDetails.add(queueDetails);
            return this;
        }

        public Builder addServiceDetails(ServiceDetails serviceDetails)
        {
            Objects.requireNonNull(serviceDetails, "serviceDetails can not be null");
            this.serviceDetails.add(serviceDetails);
            return this;
        }

        public DiscoveryReturn build()
        {
            return new DiscoveryReturn(serviceDetails, queueDetails);
        }
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
        DiscoveryReturn that = (DiscoveryReturn) o;
        return getServiceDetails().equals(that.getServiceDetails()) && getQueueDetails().equals(that.getQueueDetails());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getServiceDetails(), getQueueDetails());
    }

    @Override
    public String toString()
    {
        return "DiscoveryReturn{" +
                "serviceDetails=" + serviceDetails +
                ", queueDetails=" + queueDetails +
                '}';
    }
}
