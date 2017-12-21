package se.kodarkatten.casual.api.buffer.type.fielded

import se.kodarkatten.casual.api.buffer.type.fielded.json.CasualFieldedLookupException
import spock.lang.Specification

class FieldedTypeBufferWriteTest extends Specification
{
    def "write field once"()
    {
        expect:
        def fb = FieldedTypeBuffer.create()
                                  .write(name, v)
        v == fb.read(name).getData()
        where:
        name         | v
        'FLD_SHORT1' | (short)42
        'FLD_LONG1'  | 42l
        'FLD_CHAR2'  | (char)'a'
        'FLD_FLOAT3' | 128f
        'FLD_DOUBLE2'| 1024d
        'FLD_STRING2'| 'casually'
        'FLD_BINARY3'| 'There are 10 types of people in the world.'.bytes
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
        'FLD_SHORT1' | (short)42                                          | (short)84
        'FLD_LONG3'  | 42l                                                | 84l
        'FLD_CHAR2'  | (char)'a'                                          | (char)'b'
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
                                  .write('FLD_SHORT1', (short)42)
                                  .write('FLD_LONG1', 42l)
                                  .write('FLD_CHAR2' , (char)'a')
                                  .write('FLD_FLOAT3', 128f)
                                  .write('FLD_DOUBLE2', 1024d)
                                  .write('FLD_STRING2', 'casually')
        when:
        Short shortValue = fb.peek('FLD_SHORT1').get().getData(Short.class)
        Long longValue = fb.peek('FLD_LONG1').get().getData(Long.class)
        Character charValue = fb.peek('FLD_CHAR2').get().getData(Character.class)
        Float floatValue = fb.peek('FLD_FLOAT3').get().getData(Float.class)
        Float doubleValue = fb.peek('FLD_DOUBLE2').get().getData(Double.class)
        String stringValue = fb.peek('FLD_STRING2').get().getData(String.class)
        then:
        noExceptionThrown()
        shortValue == (short) 42
        longValue == 42l
        charValue == (char)'a'
        floatValue == 128f
        doubleValue == 1024d
        stringValue == 'casually'
    }

}
