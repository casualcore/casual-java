package se.kodarkatten.casual.api.buffer.type.fielded

import se.kodarkatten.casual.api.Utils.ResourceLoader
import se.kodarkatten.casual.api.buffer.type.fielded.json.CasualFieldedLookupException
import spock.lang.Shared
import spock.lang.Specification

class FieldedTypeBufferDecodeAndReadTest extends Specification
{
    @Shared
    def resource = '/fielded/field.bin'

    @Shared
    def data

    def setupSpec()
    {
        data = ResourceLoader.getResourceAsByteArray(resource)
        then:
        data != null
        data.length == 154
    }

    def "test decoding data"()
    {
        setup:
        def l = [data]
        when:
        def fb = FieldedTypeBuffer.create(l)
        then:
        fb != null
    }

    def "read long value"()
    {
        setup:
        def l = [data]
        when:
        def fb = FieldedTypeBuffer.create(l)
        def v = fb.read('FLD_LONG1')
        then:
        v.getData() == 1048576
    }

    def "read short value"()
    {
        setup:
        def l = [data]
        when:
        def fb = FieldedTypeBuffer.create(l)
        def v = fb.read('FLD_SHORT1')
        then:
        v.getData() == 42
    }

    def "read binary value"()
    {
        setup:
        def l = [data]
        when:
        def fb = FieldedTypeBuffer.create(l)
        def v = fb.read('FLD_BINARY1')
        def s = new String(v.getData())
        then:
        s == '123123123123'
    }

    def "read double value"()
    {
        setup:
        def l = [data]
        when:
        def fb = FieldedTypeBuffer.create(l)
        def v = fb.read('FLD_DOUBLE1')
        then:
        v.getData() == 0.023809523809523808
    }

    def "read float value"()
    {
        setup:
        def l = [data]
        when:
        def fb = FieldedTypeBuffer.create(l)
        def v = fb.read('FLD_FLOAT1')
        then:
        v.getData() == 0.023809524f
    }

    def "read char value"()
    {
        setup:
        def l = [data]
        when:
        def fb = FieldedTypeBuffer.create(l)
        def v = fb.read('FLD_CHAR1')
        then:
        v.getData() == 'a'
    }

    def "read string value"()
    {
        setup:
        def l = [data]
        when:
        def fb = FieldedTypeBuffer.create(l)
        def v = fb.read('FLD_STRING1')
        then:
        v.getData() == 'casual'
    }

    def "fail reading non existent field"()
    {
        setup:
        def l = [data]
        def name = 'NA'
        when:
        def fb = FieldedTypeBuffer.create(l)
        def v = fb.read(name)
        then:
        v == null
        def e = thrown(CasualFieldedLookupException)
        e.message == "name: ${name} does not exist with index: 0"
    }

}
