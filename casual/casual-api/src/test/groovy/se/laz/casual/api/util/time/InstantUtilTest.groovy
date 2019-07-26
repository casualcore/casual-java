/*
 * Copyright (c) 2017 - 2019, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.util.time

import spock.lang.Shared
import spock.lang.Specification

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class InstantUtilTest extends Specification
{
    @Shared
    LocalDateTime now = LocalDateTime.now()
    @Shared
    Instant instant = now.toInstant(ZoneOffset.UTC)

    def 'roundtrip'()
    {
        setup:
        long nanos = InstantUtil.toNanos(instant)
        when:
        Instant instantRecovered = InstantUtil.fromNanos(nanos)
        then:
        instant == instantRecovered
    }

}
