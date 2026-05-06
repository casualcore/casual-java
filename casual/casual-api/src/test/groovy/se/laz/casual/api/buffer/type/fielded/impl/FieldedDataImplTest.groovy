/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.impl

import se.laz.casual.api.buffer.type.fielded.FieldType
import se.laz.casual.api.buffer.type.fielded.FieldedData
import spock.lang.Specification

class FieldedDataImplTest extends Specification
{
    def "Check hashcode on maps with complex types."()
    {
        given:
        FieldedData<?> dataValue1a = FieldedDataImpl.of( v, type )
        FieldedData<?> dataValue1b = FieldedDataImpl.of( v1a, type )
        FieldedData<?> dataValue2a = FieldedDataImpl.of( v2, type )
        FieldedData<?> dataValue2b = FieldedDataImpl.of( v2, type )

        Map<String,List<FieldedData>> data1 = ["a":[dataValue1a],"c":[dataValue2a]]
        Map<String,List<FieldedData>> data2 = ["a":[dataValue1b],"c":[dataValue2b]]
        Map<String,List<FieldedData>> data3 = ["a":[dataValue2a],"c":[dataValue2a]]

        expect:
        data1.hashCode(  ) == data2.hashCode(  )
        data3.hashCode(  ) != data2.hashCode(  )

        where:
        v              | v1a            | v2             | type
        "String"       | "String"       | "String2"      | FieldType.CASUAL_FIELD_STRING
        1L             | 1L             | 2L             | FieldType.CASUAL_FIELD_LONG
        1 as short     | 1 as short     | 2 as short     | FieldType.CASUAL_FIELD_SHORT
        1 as char      | 1 as char      | 2 as char      | FieldType.CASUAL_FIELD_CHAR
        1.0 as double  | 1.0 as double  | 2.0 as double  | FieldType.CASUAL_FIELD_DOUBLE
        1.0f           | 1.0f           | 2.0f           | FieldType.CASUAL_FIELD_FLOAT
        "1".getBytes() | "1".getBytes() | "2".getBytes() | FieldType.CASUAL_FIELD_BINARY
    }
}
