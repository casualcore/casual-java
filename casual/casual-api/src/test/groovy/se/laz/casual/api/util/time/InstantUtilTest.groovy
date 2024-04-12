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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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

    def "Convert to Epoch Micro"()
    {
        when:
        Instant date = ZonedDateTime.parse( input, DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant(  )
        long actual = InstantUtil.toEpochMicro( date )

        then:
        actual == expected

        where:
        input                         || expected
        "2024-04-12T12:34:56.123456Z" || 1712925296123456
        "2024-04-12T12:34:56.123455Z" || 1712925296123455
        "2024-04-12T12:34:56.124456Z" || 1712925296124456
    }

}
