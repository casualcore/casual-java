/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.queue

import se.laz.casual.api.flags.ErrorState
import spock.lang.Shared
import spock.lang.Specification

class EnqueueReturnTest extends Specification {

    @Shared UUID someUuid = UUID.randomUUID()

    def "build ok variant"()
    {
        when:

        ErrorState stateOk = ErrorState.OK
        EnqueueReturn enqueueReturn = EnqueueReturn.createBuilder().withId(someUuid).withErrorState(stateOk).build()

        then:
        enqueueReturn.getId().get() == someUuid
        enqueueReturn.getErrorState() == stateOk
    }

    def "build non-ok variant"()
    {
        when:

        ErrorState stateTpenoent = ErrorState.TPENOENT
        EnqueueReturn enqueueReturn = EnqueueReturn.createBuilder().withErrorState(stateTpenoent).build()

        then:
        !enqueueReturn.getId().isPresent()
        enqueueReturn.getErrorState() == stateTpenoent
    }

    def "buildable variants"(UUID uuid, ErrorState errorState)
    {
        when:

        EnqueueReturn enqueueReturn = EnqueueReturn.createBuilder().withId(uuid).withErrorState(errorState).build()

        then:
        enqueueReturn != null
        noExceptionThrown()

        where:
        uuid              | errorState
        UUID.randomUUID() | ErrorState.OK
        null              | ErrorState.TPENOENT
    }

    def "not buildable variants"(UUID uuid, ErrorState errorState)
    {

        when:

        EnqueueReturn enqueueReturn = EnqueueReturn.createBuilder().withId(uuid).withErrorState(errorState).build()

        then:
        enqueueReturn == null
        thrown NullPointerException

        where:
        uuid              | errorState
        UUID.randomUUID() | null
        null              | null
    }
}
