package se.kodarkatten.casual.api.service;

import java.io.Serializable;
import java.util.Objects;

public final class ServiceInfo implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final String serviceName;
    private ServiceInfo(final String serviceName)
    {
        this.serviceName = serviceName;
    }
    public static ServiceInfo of(final String serviceName)
    {
        Objects.requireNonNull(serviceName, "serviceName can not be null");
        return new ServiceInfo(serviceName);
    }
    public String getServiceName()
    {
        return serviceName;
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
        return Objects.equals(serviceName, that.serviceName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(serviceName);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("ServiceInfo{");
        sb.append("serviceName='").append(serviceName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
