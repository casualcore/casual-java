/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException
import se.laz.casual.jca.DomainId
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class DomainIdValueHolderTest extends Specification
{
    def "only one concurrent writer can set the domain id"()
    {
        given:
        int writerCount = 16
        DomainIdValueHolder instance = DomainIdValueHolder.of()
        List<DomainId> domainIds = (1..writerCount).collect { DomainId.of(UUID.randomUUID()) }
        CyclicBarrier start = new CyclicBarrier(writerCount)
        ExecutorService executor = Executors.newFixedThreadPool(writerCount)

        when:
        List<Future<Boolean>> futures = domainIds.collect { domainId ->
            executor.submit({
                start.await(5, TimeUnit.SECONDS)
                try
                {
                    instance.accept(domainId)
                    return true
                }
                catch(CasualProtocolException expected)
                {
                    return false
                }
            } as Callable<Boolean>)
        }
        List<Boolean> results = futures.collect { it.get(5, TimeUnit.SECONDS) }

        then:
        results.count(true) == 1
        domainIds.contains(instance.get())

        cleanup:
        executor.shutdownNow()
    }
}
