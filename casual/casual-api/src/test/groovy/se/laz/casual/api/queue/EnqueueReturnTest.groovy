/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
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
        enqueueReturn.getErrorCode().isEmpty()

        when:
        EnqueueReturn anotherEnqueueReturn = EnqueueReturn.createBuilder().withId(someUuid).withErrorState(stateOk).build()
        then:
        enqueueReturn == anotherEnqueueReturn

        when:
        EnqueueReturn notEqual = EnqueueReturn.createBuilder().withId(someUuid).withErrorState(ErrorState.TPENOENT).build()

        then:
        enqueueReturn != notEqual

    }

    def "build non-ok variant"()
    {
        when:

        ErrorState stateTpenoent = ErrorState.TPENOENT
        EnqueueReturn enqueueReturn = EnqueueReturn.createBuilder().withErrorState(stateTpenoent).build()

        then:
        !enqueueReturn.getId().isPresent()
        enqueueReturn.getErrorState() == stateTpenoent
        enqueueReturn.getErrorCode().isEmpty()
    }

    def 'equality'()
    {
       when:
       EnqueueReturn enqueueReturnOne = EnqueueReturn.createBuilder()
               .withId(uuidOne)
               .withErrorState(errorStateOne)
               .withErrorCode(errorCodeOne)
               .build()
       EnqueueReturn enqueueReturnTwo = EnqueueReturn.createBuilder()
               .withId(uuidTwo)
               .withErrorState(errorStateTwo)
               .withErrorCode(errorCodeTwo)
               .build()
       then:
       (enqueueReturnOne == enqueueReturnTwo) == expectedResult
       where:
       uuidOne            | uuidTwo           | errorStateOne       | errorStateTwo       | errorCodeOne            | errorCodeTwo      | expectedResult
       someUuid           | someUuid          | ErrorState.OK       | ErrorState.OK       | QueueErrorCode.OK       | QueueErrorCode.OK | true
       UUID.randomUUID()  | someUuid          | ErrorState.OK       | ErrorState.OK       | QueueErrorCode.OK       | QueueErrorCode.OK | false
       someUuid           | UUID.randomUUID() | ErrorState.OK       | ErrorState.OK       | QueueErrorCode.OK       | QueueErrorCode.OK | false
       someUuid           | someUuid          | ErrorState.TPENOENT | ErrorState.OK       | QueueErrorCode.OK       | QueueErrorCode.OK | false
       someUuid           | someUuid          | ErrorState.OK       | ErrorState.TPENOENT | QueueErrorCode.OK       | QueueErrorCode.OK | false
       someUuid           | someUuid          | ErrorState.OK       | ErrorState.OK       | QueueErrorCode.NO_QUEUE | QueueErrorCode.OK | false
    }

    def "buildable variants"(UUID uuid, ErrorState errorState)
    {
        when:
        EnqueueReturn enqueueReturn = EnqueueReturn.createBuilder()
                .withId(uuid)
                .withErrorState(errorState)
                .withErrorCode(errorCode)
                .build()

        then:
        enqueueReturn != null
        noExceptionThrown()

        where:
        uuid              | errorState           | errorCode
        UUID.randomUUID() | ErrorState.OK        | null
        null              | ErrorState.TPENOENT  | null
        UUID.randomUUID() | ErrorState.OK        | QueueErrorCode.OK
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
