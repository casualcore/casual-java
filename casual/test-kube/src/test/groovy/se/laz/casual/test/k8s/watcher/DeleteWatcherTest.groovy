/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.watcher

import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.client.Watcher
import se.laz.casual.test.k8s.watchers.DeleteWatcher
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class DeleteWatcherTest extends Specification
{
    DeleteWatcher<Pod> instance

    def setup()
    {
        instance = new DeleteWatcher<>()
    }

    def "Wait for delete."()
    {
        given:
        CountDownLatch complete = new CountDownLatch(1 )

        when:
        CompletableFuture<Void> future = CompletableFuture.supplyAsync( ()->{ instance.waitUntilDeleted(); complete.countDown(  ) } )
        complete.await( 1, TimeUnit.MILLISECONDS )

        then:
        !future.isDone(  )

        when:
        instance.eventReceived( Watcher.Action.DELETED, Mock( Pod ) )
        complete.await()

        then:
        future.isDone()
    }

    def "Wait for delete with timeout, false."()
    {
        given:
        CountDownLatch complete = new CountDownLatch(1 )

        when:
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync( ()->{
            Boolean result = instance.waitUntilDeleted(1, TimeUnit.MILLISECONDS)
            complete.countDown(  )
            return result
        } )

        complete.await()

        then:
        !future.get()
    }

    def "Wait for delete with timeout, true."()
    {
        given:
        CountDownLatch started = new CountDownLatch( 1 )
        CountDownLatch start = new CountDownLatch( 1 )
        CountDownLatch complete = new CountDownLatch(1 )

        when:
        CompletableFuture<Boolean> task = CompletableFuture.supplyAsync(   ()-> {
            started.countDown(  )
            start.await()
            Boolean result = instance.waitUntilDeleted(1, TimeUnit.MILLISECONDS)
            complete.countDown(  )
            return result
        })

        and:
        started.await()
        instance.eventReceived( Watcher.Action.DELETED, Mock( Pod ) )
        start.countDown(  )
        complete.await()

        Boolean result = task.get( 1, TimeUnit.SECONDS)

        then:
        result
    }

}
