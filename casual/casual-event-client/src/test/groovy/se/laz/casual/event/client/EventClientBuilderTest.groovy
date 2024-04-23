/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.client

import spock.lang.Specification

class EventClientBuilderTest extends Specification
{
    def 'failed construction'()
    {
        when:
        EventClientBuilder.createBuilder()
                .withHost(host)
                .withPort(port)
                .withEventObserver (eventObserver)
                .withConnectionObserver(connectionObserver)
                .build()
        then:
        thrown(NullPointerException)
        where:
        host                           || port                   || eventObserver || connectionObserver
        null                           || 7789                   || {}            || {}
        'localhost'                    || null                   || {}            || {}
        'localhost'                    || 7789                   || null          || {}
        'localhost'                    || 7789                   || {}            || null
    }
}
