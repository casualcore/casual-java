/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded

import se.laz.casual.api.Utils.ResourceLoader
import spock.lang.Shared
import spock.lang.Specification

class RoundTripTest extends Specification
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

    def "decode, encode, decode again"()
    {
        def l = [data]
        expect:
        def fb1 = FieldedTypeBuffer.create(l)
        def fb2 = FieldedTypeBuffer.create(fb1.encode())
        fb1.read(name) == fb2.read(name2)
        where:
        name         |name2
        'FLD_SHORT1' |'FLD_SHORT1'
        'FLD_LONG1'  |'FLD_LONG1'
        'FLD_CHAR1'  |'FLD_CHAR1'
        'FLD_FLOAT1' |'FLD_FLOAT1'
        'FLD_DOUBLE1'|'FLD_DOUBLE1'
        'FLD_STRING1'|'FLD_STRING1'
        'FLD_BINARY1'|'FLD_BINARY1'
    }

    def "decode, encode, decode again - compare type buffers"()
    {
        setup:
        def l = [data]
        when:
        def fb1 = FieldedTypeBuffer.create(l)
        def fb2 = FieldedTypeBuffer.create(fb1.encode())
        then:
        fb1 != null
        fb2 == fb1
    }

}
