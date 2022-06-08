/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.config;

import java.util.Objects;

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

    private Outbound(String managedExecutorServiceName, int numberOfThreads, boolean unmanaged)
    {
        this.managedExecutorServiceName = managedExecutorServiceName;
        this.numberOfThreads = numberOfThreads;
        this.unmanaged = unmanaged;
    }

    public static Outbound of(Boolean unmanaged)
    {
        return new Outbound(DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME, DEFAULT_NUMBER_OF_THREADS, null == unmanaged ? DEFAULT_UNMANAGED : unmanaged);
    }

    public static Outbound of(String managedExecutorServiceName, Integer numberOfThreads)
    {
        return of(managedExecutorServiceName, numberOfThreads, DEFAULT_UNMANAGED);
    }

    public static Outbound of(String managedExecutorServiceName, Integer numberOfThreads, Boolean unmanaged)
    {
        return new Outbound(null == managedExecutorServiceName ? DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME :  managedExecutorServiceName,
                            null == numberOfThreads ? DEFAULT_NUMBER_OF_THREADS : numberOfThreads,
                            null == unmanaged ? DEFAULT_UNMANAGED : unmanaged);
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
        return getNumberOfThreads() == outbound.getNumberOfThreads() && getUnmanaged() == outbound.getUnmanaged() && Objects.equals(getManagedExecutorServiceName(), outbound.getManagedExecutorServiceName());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getManagedExecutorServiceName(), getNumberOfThreads(), getUnmanaged());
    }

    @Override
    public String toString()
    {
        return "Outbound{" +
                "managedExecutorServiceName='" + managedExecutorServiceName + '\'' +
                ", numberOfThreads=" + numberOfThreads +
                ", unmanaged=" + unmanaged +
                '}';
    }
}
