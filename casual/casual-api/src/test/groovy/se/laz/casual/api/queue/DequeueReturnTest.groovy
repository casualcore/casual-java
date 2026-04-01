/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.queue


import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.flags.ErrorState
import spock.lang.Shared
import spock.lang.Specification

class DequeueReturnTest extends Specification {

    @Shared String message = '{"hello": 1}'
    @Shared QueueMessage someQueueMessage = QueueMessage.of(JsonBuffer.of(message))

    def "build ok variant"()
    {
        when:
        ErrorState stateOk = ErrorState.OK
        DequeueReturn dequeueReturn = DequeueReturn.createBuilder().withQueueMessage(someQueueMessage).withErrorState(stateOk).build()

        then:
        Optional<QueueMessage> returnedMessages = dequeueReturn.getQueueMessage()
        returnedMessages.isPresent()
        new String(returnedMessages.get().getPayload().getBytes().get(0)) == message
        dequeueReturn.getErrorState() == stateOk
        dequeueReturn.getErrorCode().isEmpty()
        when:
        DequeueReturn dequeueReturnTwo = DequeueReturn.createBuilder().withQueueMessage(someQueueMessage).withErrorState(stateOk).build()
        then:
        dequeueReturn == dequeueReturnTwo
        when:
        DequeueReturn dequeueReturnThree = DequeueReturn.createBuilder().withQueueMessage(someQueueMessage).withErrorState(ErrorState.TPENOENT).build()
        then:
        dequeueReturnThree != dequeueReturn
    }

    def "build non-ok variant"()
    {
        when:
        ErrorState stateTpenoent = ErrorState.TPENOENT
        DequeueReturn dequeueReturn = DequeueReturn.createBuilder().withErrorState(stateTpenoent).build()

        then:
        !dequeueReturn.getQueueMessage().isPresent()
        dequeueReturn.getErrorState() == stateTpenoent
        dequeueReturn.getErrorCode().isEmpty()
    }

    def "buildable variants"(QueueMessage queueMessage, ErrorState errorState)
    {
        when:
        DequeueReturn dequeueReturn = DequeueReturn.createBuilder().
                withQueueMessage(queueMessage).
                withErrorState(errorState).
                withErrorCode(errorCode).
                build()

        then:
        noExceptionThrown()
        dequeueReturn != null
        if(null != errorCode)
        {
           dequeueReturn.getErrorCode().isPresent()
        }

        where:
        queueMessage     | errorState           | errorCode
        someQueueMessage | ErrorState.OK        | null
        null             | ErrorState.OK        | null
        someQueueMessage | ErrorState.TPENOENT  | null
        null             | ErrorState.TPENOENT  | null
        someQueueMessage | ErrorState.OK        | QueueErrorCode.OK
        null             | ErrorState.OK        | QueueErrorCode.NO_MESSAGE
    }

    def "not buildable variants"(QueueMessage queueMessages, ErrorState errorState)
    {

        when:
        DequeueReturn dequeueReturn = DequeueReturn.createBuilder().withQueueMessage(queueMessages).withErrorState(errorState).build()

        then:
        dequeueReturn == null
        thrown NullPointerException

        where:
        queueMessages    | errorState
        someQueueMessage | null
        null             | null
    }
}
