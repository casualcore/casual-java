/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded

import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedTypeBufferProcessor
import se.laz.casual.api.testdata.SimpleListPojo
import spock.lang.Shared
import spock.lang.Specification

class FieldedTypeBufferExtractReplaceTest extends Specification
{
    @Shared
    SimpleListPojo slp = SimpleListPojo.of(['sometimes', 'when', 'you', 'fall', 'you', 'fly'], [1, 2, 3, 4, 5])

    def 'extract and replace'()
    {
        setup:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(slp)
        FieldedTypeBuffer validateAgainst = FieldedTypeBufferProcessor.marshall(slp)
        when:
        FieldedTypeBuffer replacementBuffer = FieldedTypeBuffer.create()
                                                               .replace(b.extract())
        then:
        noExceptionThrown()
        b.isEmpty()
        validateAgainst == replacementBuffer
    }

    def 'copy constructor'()
    {
        setup:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(slp)
        FieldedTypeBuffer copy = FieldedTypeBuffer.of(b)
        when:
        def mapB = b.extract()
        //assert !copy.isEmpty()
        mapB.clear()
        //assert !copy.isEmpty()
        then:
        noExceptionThrown()
        !copy.isEmpty()
        copy.extract() != mapB
    }

}
