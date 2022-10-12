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
    // In netty number of threads == 0 is interpreted as:
    // Math.max(1, SystemPropertyUtil.getInt( "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2))
    // for the EventLoopGroup
    private static final int DEFAULT_NUMBER_OF_THREADS = 0;
    private static final boolean DEFAULT_UNMANAGED = false;

    private final String managedExecutorServiceName;
    private int numberOfThreads;
    private boolean unmanaged;
    private Boolean useEpoll;

    public static final String USE_EPOLL_ENV_VAR_NAME = "CASUAL_OUTBOUND_USE_EPOLL";

    private Outbound(String managedExecutorServiceName, int numberOfThreads, boolean unmanaged, Boolean useEpoll)
    {
        this.managedExecutorServiceName = managedExecutorServiceName;
        this.numberOfThreads = numberOfThreads;
        this.unmanaged = unmanaged;
        this.useEpoll = useEpoll;
    }

    private boolean getUseEPollFromEnv()
    {
        return Boolean.valueOf(Optional.ofNullable(System.getenv(USE_EPOLL_ENV_VAR_NAME)).orElse("false"));
    }

    public static Outbound of()
    {
        return new Outbound(DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME, DEFAULT_NUMBER_OF_THREADS, DEFAULT_UNMANAGED, null);
    }

    public static Outbound of(Boolean unmanaged)
    {
        return new Outbound(DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME, DEFAULT_NUMBER_OF_THREADS, null == unmanaged ? DEFAULT_UNMANAGED : unmanaged, null);
    }

    public static Outbound of(String managedExecutorServiceName, Integer numberOfThreads)
    {
        return of(managedExecutorServiceName, numberOfThreads, DEFAULT_UNMANAGED, null);
    }

    public static Outbound of(String managedExecutorServiceName, Integer numberOfThreads, Boolean unmanaged)
    {
        return of(managedExecutorServiceName, numberOfThreads, unmanaged, null);
    }

    public static Outbound of(String managedExecutorServiceName, Integer numberOfThreads, Boolean unmanaged, Boolean useEpoll)
    {
        return new Outbound(null == managedExecutorServiceName ? DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME :  managedExecutorServiceName,
                            null == numberOfThreads ? DEFAULT_NUMBER_OF_THREADS : numberOfThreads,
                            null == unmanaged ? DEFAULT_UNMANAGED : unmanaged,
                            useEpoll);
    }

    public String getManagedExecutorServiceName()
    {
        return managedExecutorServiceName == null ? DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME : managedExecutorServiceName;
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
                ", numberOfThreads=" + getNumberOfThreads() +
                ", unmanaged=" + getUnmanaged() +
                ", useEpoll=" + getUseEpoll() +
                '}';
    }

}
