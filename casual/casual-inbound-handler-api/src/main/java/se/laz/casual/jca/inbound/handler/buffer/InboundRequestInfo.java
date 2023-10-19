/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.buffer;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.Optional;

public final class InboundRequestInfo
{
    private final Method proxyMethod;
    private final Method realMethod;
    private final String serviceName;
    private final Proxy proxy;

    private InboundRequestInfo(Method proxyMethod, Method realMethod, String serviceName, Proxy proxy)
    {
        this.proxyMethod = proxyMethod;
        this.realMethod = realMethod;
        this.serviceName = serviceName;
        this.proxy = proxy;
    }

    private InboundRequestInfo(Builder builder)
    {
        this(builder.proxyMethod, builder.realMethod, builder.serviceName, builder.proxy);
    }

    public Optional<Method> getProxyMethod()
    {
        return Optional.ofNullable(proxyMethod);
    }

    public Optional<Method> getRealMethod()
    {
        return Optional.ofNullable(realMethod);
    }

    public Optional<String> getServiceName()
    {
        return Optional.ofNullable(serviceName);
    }

    public Proxy getProxy()
    {
        return proxy;
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
        InboundRequestInfo that = (InboundRequestInfo) o;
        return Objects.equals(getProxyMethod(), that.getProxyMethod()) && Objects.equals(getRealMethod(), that.getRealMethod()) && Objects.equals(getServiceName(), that.getServiceName()) && Objects.equals(getProxy(), that.getProxy());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getProxyMethod(), getRealMethod(), getServiceName(), getProxy());
    }

    @Override
    public String toString()
    {
        return "InboundRequestInfo{" +
                "proxyMethod=" + proxyMethod +
                ", realMethod=" + realMethod +
                ", serviceName='" + serviceName + '\'' +
                ", proxy=" + proxy +
                '}';
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private Method proxyMethod;
        private Method realMethod;
        private String serviceName;
        private Proxy proxy;

        private Builder()
        {}

        public Builder withProxyMethod(Method proxyMethod)
        {
            this.proxyMethod = proxyMethod;
            return this;
        }

        public Builder withRealMethod(Method realMethod)
        {
            this.realMethod = realMethod;
            return this;
        }

        public Builder withServiceName(String serviceName)
        {
            this.serviceName = serviceName;
            return this;
        }

        public Builder withProxy(Proxy proxy)
        {
            this.proxy = proxy;
            return this;
        }

        public InboundRequestInfo build()
        {
            Objects.requireNonNull(proxy, "proxy can not be null");
            return new InboundRequestInfo(this);
        }
    }
}
