/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded

import se.laz.casual.api.Utils.ResourceLoader
import se.laz.casual.api.buffer.type.fielded.json.CasualFieldedLookupException
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

    def 'read index out of bounds'()
    {
        setup:
        def l = [data]
        def name = 'FLD_STRING1'
        def index = 1
        when:
        def b = FieldedTypeBuffer.create(l)
        def v = b.read(name, index)
        then:
        v == null
        def e = thrown(CasualFieldedLookupException)
        e.message == "name: ${name} does not exist with index: ${index}"
    }

    def "peek non existent field"()
    {
        setup:
        def l = [data]
        def name = 'NA'
        when:
        def fb = FieldedTypeBuffer.create(l)
        def v = fb.peek(name, 0)
        then:
        noExceptionThrown()
        !v.isPresent()
    }

    def 'peek index out of bounds'()
    {
        setup:
        def l = [data]
        def name = 'FLD_STRING1'
        def index = 1
        when:
        def b = FieldedTypeBuffer.create(l)
        def v = b.peek(name, index)
        then:
        noExceptionThrown()
        !v.isPresent()
    }

    def 'readAll missing key'()
    {
        setup:
        def l = [data]
        when:
        def b = FieldedTypeBuffer.create(l)
        def r = b.readAll('NOT_THERE')
        then:
        noExceptionThrown()
        r.isEmpty()
    }

    def 'create with no payload'()
    {
        when:
        def b = FieldedTypeBuffer.create([])
        then:
        noExceptionThrown()
        b.isEmpty() == true
    }

    def 'create with 0 len byte[]'()
    {
        when:
        def data = new byte[0]
        def b = FieldedTypeBuffer.create([data])
        then:
        noExceptionThrown()
        data.length == 0
        b.isEmpty() == true
    }

}
