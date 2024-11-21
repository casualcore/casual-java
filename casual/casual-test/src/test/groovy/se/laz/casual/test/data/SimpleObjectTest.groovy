/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.data

import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedTypeBufferProcessor
import spock.lang.Specification

class SimpleObjectTest extends Specification
{
    Long id = 1L
    String name = "myname"
    SimpleObject instance

    def setup()
    {
        instance = new SimpleObject( id, name )
    }

    def "Marshall too and from fielded."()
    {
        when:
        FieldedTypeBuffer buffer = FieldedTypeBufferProcessor.marshall( instance )

        then:
        buffer != null

        when:
        SimpleObject actual = FieldedTypeBufferProcessor.unmarshall( buffer, SimpleObject.class )

        then:
        actual == instance
    }
}
