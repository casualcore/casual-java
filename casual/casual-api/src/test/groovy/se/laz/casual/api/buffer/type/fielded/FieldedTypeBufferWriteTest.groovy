/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded

import se.laz.casual.api.buffer.type.fielded.json.CasualFieldedLookupException
import spock.lang.Specification

class FieldedTypeBufferWriteTest extends Specification
{
    def "write field once"()
    {
        expect:
        def fb = FieldedTypeBuffer.create(FieldedTypeBuffer.create()
                                                           .write(name, v)
                                                           .encode())
        v == fb.read(name).getData()
        where:
        name         | v
        'FLD_SHORT1' | 42 as short
        'FLD_LONG1'  | 42l
        'FLD_LONG2'  | 42 as int
        'FLD_CHAR1'  | '0' as char
        'FLD_CHAR2'  | 'ö' as char
        'FLD_FLOAT3' | 128f
        'FLD_DOUBLE2'| 1024d
        'FLD_STRING2'| 'casually'
        'FLD_BINARY3'| 'There are 10 types of people in the world.'.bytes
    }

    def 'boolean test'()
    {
        expect:
        def fb = FieldedTypeBuffer.create(FieldedTypeBuffer.create()
                                                           .write(name, v)
                                                           .encode())
        char c = fb.read(name).getData(Character.class)
        expectation == (0 == Character.getNumericValue(c))
        where:
        name         | v           | expectation
        'FLD_CHAR1'  | '0' as char | true
        'FLD_CHAR2'  | 'ö' as char | false
    }

    def "write field twice"()
    {
        expect:
        def fb = FieldedTypeBuffer.create()
                                  .write(name, v1)
                                  .write(name, v2)
        v1 == fb.read(name).getData()
        v2 == fb.read(name, 1).getData()
        where:
        name         | v1                                                 | v2
        'FLD_SHORT1' | 42 as short                                        | 84 as short
        'FLD_LONG3'  | 42l                                                | 84l
        'FLD_LONG2'  | 42                                                 | 84
        'FLD_CHAR2'  | 'a' as char                                        | 'b' as char
        'FLD_FLOAT3' | 128f                                               | 256f
        'FLD_DOUBLE2'| 1024d                                              | 2048d
        'FLD_STRING2'| 'casually'                                         | 'casual'
        'FLD_BINARY3'| 'There are 10 types of people in the world.'.bytes | "Those who understand binary and those who don't".bytes
    }

    def "fail writing double as short"()
    {
        setup:
        Double v = 42
        def name = 'FLD_SHORT1'
        when:
        def fb = FieldedTypeBuffer.create()
                                  .write(name, v)
        then:
        fb == null
        def e = thrown(CasualFieldedLookupException)
        e.message == 'class: class java.lang.Double is not compatible with field class: class java.lang.Short'
    }

    def "fail writing double as float"()
    {
        setup:
        Double v = 42
        def name = 'FLD_SHORT1'
        when:
        def fb = FieldedTypeBuffer.create()
                                  .write(name, v)
        then:
        fb == null
        def e = thrown(CasualFieldedLookupException)
        e.message == 'class: class java.lang.Double is not compatible with field class: class java.lang.Short'
    }

    def "fail writing short as long"()
    {
        setup:
        Short v = 42
        def name = 'FLD_LONG1'
        when:
        def fb = FieldedTypeBuffer.create()
                                  .write(name, v)
        then:
        fb == null
        def e = thrown(CasualFieldedLookupException)
        e.message == 'class: class java.lang.Short is not compatible with field class: class java.lang.Long'
    }

    def "fail writing float as double"()
    {
        setup:
        Float v = 42f
        def name = 'FLD_DOUBLE2'
        when:
        def fb = FieldedTypeBuffer.create()
                                  .write(name, v)
        then:
        fb == null
        def e = thrown(CasualFieldedLookupException)
        e.message == 'class: class java.lang.Float is not compatible with field class: class java.lang.Double'
    }

    def 'casting'()
    {
        setup:
        def fb = FieldedTypeBuffer.create()
                                  .write('FLD_SHORT1', 42 as short)
                                  .write('FLD_LONG1', 42l)
                                  .write('FLD_CHAR2' , 'ö' as char)
                                  .write('FLD_FLOAT3', 128f)
                                  .write('FLD_DOUBLE2', 1024d)
                                  .write('FLD_STRING2', 'casually')
        when:
        Short shortValue = fb.peek('FLD_SHORT1').get().getData(Short.class)
        Long longValue = fb.peek('FLD_LONG1').get().getData(Long.class)
        Integer intValue = fb.peek('FLD_LONG1').get().getData(Integer.class)
        Character charValue = fb.peek('FLD_CHAR2').get().getData(Character.class)
        Float floatValue = fb.peek('FLD_FLOAT3').get().getData(Float.class)
        Float doubleValue = fb.peek('FLD_DOUBLE2').get().getData(Double.class)
        String stringValue = fb.peek('FLD_STRING2').get().getData(String.class)
        then:
        noExceptionThrown()
        shortValue == 42 as short
        longValue == 42l
        intValue == 42
        charValue == 'ö' as char
        floatValue == 128f
        doubleValue == 1024d
        stringValue == 'casually'
    }

}
