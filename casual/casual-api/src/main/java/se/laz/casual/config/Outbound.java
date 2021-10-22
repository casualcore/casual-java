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
    private final String managedExecutorServiceName;
    private int numberOfThreads;

    private Outbound(String managedExecutorServiceName, int numberOfThreads)
    {
        this.managedExecutorServiceName = managedExecutorServiceName;
        this.numberOfThreads = numberOfThreads;
    }

    public static Outbound of(Optional<String> managedExecutorServiceName, Optional<Integer> numberOfThreads)
    {
        Objects.requireNonNull(managedExecutorServiceName, "managedExecutorServiceName can not be null");
        Objects.requireNonNull(numberOfThreads, "numberOfThreads can not be null");
        return new Outbound(managedExecutorServiceName.orElse(DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME), numberOfThreads.orElse(DEFAULT_NUMBER_OF_THREADS));
    }

    public String getManagedExecutorServiceName()
    {
        return managedExecutorServiceName == null ? DEFAULT_MANAGED_EXECUTOR_SERVICE_NAME : managedExecutorServiceName;
    }

    public int getNumberOfThreads()
    {
        return numberOfThreads;
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
        return getNumberOfThreads() == outbound.getNumberOfThreads() && Objects.equals(getManagedExecutorServiceName(), outbound.getManagedExecutorServiceName());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getManagedExecutorServiceName(), getNumberOfThreads());
    }

    @Override
    public String toString()
    {
        return "Outbound{" +
                "managedExecutorService='" + getManagedExecutorServiceName() + '\'' +
                ", numberOfThreads=" + getNumberOfThreads() +
                '}';
    }
}
