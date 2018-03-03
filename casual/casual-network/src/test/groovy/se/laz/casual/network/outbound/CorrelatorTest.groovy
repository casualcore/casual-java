/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class CorrelatorTest extends Specification
{
    @Shared
    def instance = CorrelatorImpl.of()

    def 'initial empty'()
    {
        expect:
        instance.isEmpty()
    }

    def 'with request'()
    {
        when:
        instance.put(UUID.randomUUID(), new CompletableFuture<Object>())
        then:
        !instance.isEmpty()
    }
}
