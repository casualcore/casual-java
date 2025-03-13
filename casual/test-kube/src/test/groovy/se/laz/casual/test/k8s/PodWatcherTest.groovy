/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s

import io.fabric8.kubernetes.client.Watcher
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PodWatcherTest extends Specification
{
    PodWatcher instance

    ExecutorService executor = Executors.newSingleThreadExecutor()

    def setup()
    {
        instance = new PodWatcher()
    }

    def "Wait for delete."()
    {
        when:
        executor.submit( ()->{ instance.waitUntilDeleted() } )
        instance.eventReceived( Watcher.Action.DELETED, Mock( ) )

        then:
        noExceptionThrown(  )
    }

}
