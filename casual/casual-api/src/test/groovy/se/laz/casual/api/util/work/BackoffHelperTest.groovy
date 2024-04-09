/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.util.work

import spock.lang.Specification

class BackoffHelperTest extends Specification {
    def setup() {}

    def 'maxBackoffMillis is set correctly on construction'()
    {
        given:
        long maxBackoff = 10000000000
        BackoffHelper backoffHelper = BackoffHelper.of(maxBackoff)

        expect:
        backoffHelper.getMaxBackoffMillis() == maxBackoff
    }

    def 'first failure has backoff 1/10th of maxBackoff'()
    {
        given:
        long maxBackoff = 2500
        long expectedBackoff = maxBackoff.intdiv(10)
        BackoffHelper backoffHelper = BackoffHelper.of(maxBackoff)

        when:
        long currentBackoff = backoffHelper.registerFailure()

        then:
        currentBackoff == expectedBackoff
    }

    def 'backoff increases for first 10 failures'()
    {
        given:
        long maxBackoff = 1000
        BackoffHelper backoffHelper = BackoffHelper.of(maxBackoff)
        List<Long> backoffList = []

        when:
        // Record backoff for 10 failures
        (1..10).each({
            backoffList << backoffHelper.registerFailure()
        })

        then:
        // Compare each failure with the one before it
        (1..9).each({
            backoffList[it-1] < backoffList[it]
        })
    }

    def 'backoff does not increase from 10th failure and onward'()
    {
        given:
        long maxBackoff = 20000
        BackoffHelper backoffHelper = BackoffHelper.of(maxBackoff)
        List<Long> backoffList = []

        when:
        (1..9).each({
            // These should be increasing
            backoffHelper.registerFailure()
        })

        // Get some more failure backoffs
        (1..25).each({
            backoffList << backoffHelper.registerFailure()
        })

        then:
        // Compare each failure with the one before it
        (1..25).each({
            backoffList[it-1] == backoffList[it]
        })
    }

    def 'backoffs are 10th, 9th, 8th and so on of maxBackoff for the first 10 failures'()
    {
        given:
        long maxBackoff = 31415926525
        BackoffHelper backoffHelper = BackoffHelper.of(maxBackoff)

        expect:
        (10..1).each({
            backoffHelper.registerFailure() == maxBackoff.intdiv(it)
        })
    }
}
