/*
 * Copyright (c) 2022, The casual project. All rights reserved.
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
    }

    def "build non-ok variant"()
    {
        when:
        ErrorState stateTpenoent = ErrorState.TPENOENT
        DequeueReturn dequeueReturn = DequeueReturn.createBuilder().withErrorState(stateTpenoent).build()

        then:
        !dequeueReturn.getQueueMessage().isPresent()
        dequeueReturn.getErrorState() == stateTpenoent
    }

    def "buildable variants"(QueueMessage queueMessage, ErrorState errorState)
    {
        when:
        DequeueReturn dequeueReturn = DequeueReturn.createBuilder().withQueueMessage(queueMessage).withErrorState(errorState).build()

        then:
        noExceptionThrown()
        dequeueReturn != null

        where:
        queueMessage     | errorState
        someQueueMessage | ErrorState.OK
        null             | ErrorState.OK
        someQueueMessage | ErrorState.TPENOENT
        null             | ErrorState.TPENOENT
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
