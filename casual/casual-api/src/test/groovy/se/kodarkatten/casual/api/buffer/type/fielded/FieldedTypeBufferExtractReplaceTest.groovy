package se.kodarkatten.casual.api.buffer.type.fielded

import se.kodarkatten.casual.api.buffer.type.fielded.marshalling.FieldedTypeBufferProcessor
import se.kodarkatten.casual.api.testdata.SimpleListPojo
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
}
