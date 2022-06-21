/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.config;

import java.util.Objects;
import java.util.Optional;

public final class Outbound
{
    private static final String DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME = "java:comp/DefaultManagedExecutorService";
    private static final String DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_NAME = "java:comp/DefaultManagedScheduledExecutorService";
    // In netty number of threads == 0 is interpreted as:
    // Math.max(1, SystemPropertyUtil.getInt( "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2))
    // for the EventLoopGroup
    private static final int DEFAULT_NUMBER_OF_THREADS = 0;
    private static final boolean DEFAULT_UNMANAGED = false;

    private final String managedExecutorServiceName;
    private String managedScheduledExecutorServiceName;
    private int numberOfThreads;
    private boolean unmanaged;
    private Boolean useEpoll;


    public static final String USE_EPOLL_ENV_VAR_NAME = "CASUAL_OUTBOUND_USE_EPOLL";

    private Outbound(Builder builder)
    {
        managedExecutorServiceName = builder.managedExecutorServiceName;
        numberOfThreads = builder.numberOfThreads;
        unmanaged = builder.unmanaged;
        useEpoll = builder.useEpoll;
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    private boolean getUseEPollFromEnv()
    {
        return Boolean.valueOf(Optional.ofNullable(System.getenv(USE_EPOLL_ENV_VAR_NAME)).orElse("false"));
    }

    public String getManagedExecutorServiceName()
    {
        return null == managedExecutorServiceName ? DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME : managedExecutorServiceName;
    }

    public String getManagedScheduledExecutorServiceName()
    {
        return managedScheduledExecutorServiceName == null ? DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_NAME : managedScheduledExecutorServiceName;
    }

    public int getNumberOfThreads()
    {
        return numberOfThreads;
    }

    public boolean getUnmanaged()
    {
        return unmanaged;
    }

    public boolean getUseEpoll()
    {
        return null == useEpoll ?  getUseEPollFromEnv() : useEpoll;
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
        Outbound outbound = (Outbound) o;
        return getNumberOfThreads() == outbound.getNumberOfThreads() &&
                getUnmanaged() == outbound.getUnmanaged() &&
                Objects.equals(getManagedExecutorServiceName(), outbound.getManagedExecutorServiceName()) &&
                getUseEpoll() ==  outbound.getUseEpoll();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getManagedExecutorServiceName(), getNumberOfThreads(), getUnmanaged(), getUseEpoll());
    }

    @Override
    public String toString()
    {
        return "Outbound{" +
                "managedExecutorServiceName='" + getManagedExecutorServiceName() + '\'' +
                ", managedScheduledExecutorServiceName='" + getManagedScheduledExecutorServiceName() + '\'' +
                ", numberOfThreads=" + getNumberOfThreads() +
                ", unmanaged=" + getUnmanaged() +
                ", useEpoll=" + getUseEpoll() +
                '}';
    }

    public static final class Builder
    {
        private String managedExecutorServiceName;
        private String managedScheduledExecutorServiceName;
        private Integer numberOfThreads;
        private Boolean unmanaged;
        private Boolean useEpoll;

        public Builder withManagedExecutorServiceName(String managedExecutorServiceName)
        {
            this.managedExecutorServiceName = managedExecutorServiceName;
            return this;
        }

        public Builder withManagedScheduledExecutorServiceName(String managedScheduledExecutorServiceName)
        {
            this.managedScheduledExecutorServiceName = managedScheduledExecutorServiceName;
            return this;
        }

        public Builder withNumberOfThreads(Integer numberOfThreads)
        {
            this.numberOfThreads = numberOfThreads;
            return this;
        }

        public Builder withUnmanaged(Boolean unmanaged)
        {
            this.unmanaged = unmanaged;
            return this;
        }

        public Builder withUseEpoll(Boolean useEpoll)
        {
            this.useEpoll = useEpoll;
            return this;
        }

        public Outbound build()
        {
            managedExecutorServiceName = null == managedExecutorServiceName ? DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME : managedExecutorServiceName;
            managedScheduledExecutorServiceName = managedScheduledExecutorServiceName == null ? DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_NAME : managedScheduledExecutorServiceName;
            numberOfThreads = null == numberOfThreads ? DEFAULT_NUMBER_OF_THREADS : numberOfThreads;
            unmanaged = null == unmanaged ? DEFAULT_UNMANAGED : unmanaged;
            return new Outbound(this);
        }
    }

}
