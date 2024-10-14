/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound

import se.laz.casual.config.ConfigurationOptions
import se.laz.casual.config.ConfigurationService
import spock.lang.Specification

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

class JEEConcurrencyFactoryTest extends Specification {
    def setup()
    {
        JEEConcurrencyFactory.resetScheduler()
    }

    def cleanup()
    {
        ConfigurationService.reload(  )
    }

    def cleanupSpec()
    {
        JEEConcurrencyFactory.resetScheduler()
    }

    def 'java util concurrency scheduler uses default threads when no config env is set'()
    {
        given:
        ScheduledExecutorService scheduler = JEEConcurrencyFactory.getSharedJavaUtilScheduledExecutor()

        expect:
        ((ScheduledThreadPoolExecutor) scheduler).getCorePoolSize() == JEEConcurrencyFactory.DEFAULT_CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE
    }

    def 'java util concurrency scheduler uses environment configured threads with valid config'()
    {
        given:
        int configuredPoolSize = 1579
        ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE, configuredPoolSize )

        ScheduledExecutorService scheduler = JEEConcurrencyFactory.getSharedJavaUtilScheduledExecutor()

        expect:
        ((ScheduledThreadPoolExecutor) scheduler).getCorePoolSize() == configuredPoolSize
    }

    def 'multiple calls for shared java util concurrency scheduler yields same instance'()
    {
        given:
        ScheduledExecutorService schedulerA = JEEConcurrencyFactory.getSharedJavaUtilScheduledExecutor()
        ScheduledExecutorService schedulerB = JEEConcurrencyFactory.getSharedJavaUtilScheduledExecutor()

        expect:
        schedulerA == schedulerB
    }
}
