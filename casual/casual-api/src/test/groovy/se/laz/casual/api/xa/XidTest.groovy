/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.xa

import spock.lang.Specification

import javax.transaction.xa.Xid
import java.nio.ByteBuffer

/**
 * Created by aleph on 2017-03-28.
 */
class XidTest extends Specification
{
    def "Lying about data size"()
    {
        setup:
        byte[] data = (0..XID.MAX_XID_DATA_SIZE+1) as byte[]
        def gtridLength = 1
        def bqualLength = XID.MAX_XID_DATA_SIZE
        def formatType = 42l
        when:
        Xid id = XID.of(gtridLength, bqualLength, data, formatType)
        then:
        null == id
        XIDException e = thrown()
        e.getMessage() == "(gtridLength + bqualLength) != data.length (${gtridLength} + ${bqualLength}) != ${data.length}"
    }

    def "Exceeding data size - gtrid, bqual byte[]"()
    {
        setup:
        byte[] gtridData = (0..XID.MAX_XID_DATA_SIZE/2 + 1) as byte[]
        byte[] bqualData = (0..XID.MAX_XID_DATA_SIZE/2) as byte[]
        def gtridLength = gtridData.length
        def bqualLength = bqualData.length
        def formatType = 42l
        when:
        Xid id = XID.of(gtridData, bqualData, formatType)
        then:
        null == id
        XIDException e = thrown()
        e.getMessage() == "xid total length > MAX_XID_DATA_SIZE ${gtridLength + bqualLength} > ${XID.MAX_XID_DATA_SIZE}"
    }

    def "nullformat but with data"()
    {
        setup:
        byte[] gtridData = (0..XID.MAX_XID_DATA_SIZE/2 - 1) as byte[]
        byte[] bqualData = (0..XID.MAX_XID_DATA_SIZE/2 - 1) as byte[]
        def gtridLength = gtridData.length
        def bqualLength = bqualData.length
        def formatType = XIDFormatType.NULL
        when:
        Xid id = XID.of(gtridData, bqualData, formatType.type)
        then:
        null == id
        XIDException e = thrown()
        e.getMessage() == "You can not create a NULL XID with: gtrid length ${gtridLength } and bqual length ${bqualLength}"
    }

    def "Only gtrid"()
    {
        setup:
        byte[] gtridData = (0..XID.MAX_XID_DATA_SIZE/2 + 1) as byte[]
        byte[] bqualData = null
        def formatType = 42l
        when:
        Xid id = XID.of(gtridData, bqualData, formatType)
        then:
        id != null
        id.getGlobalTransactionId() == gtridData
        id.getBranchQualifier() == null
        id.getFormatId() == formatType
    }

    def "Only bqual"()
    {
        setup:
        byte[] bqualData = (0..XID.MAX_XID_DATA_SIZE/2 + 1) as byte[]
        byte[] gtridData = null
        def formatType = 42l
        when:
        Xid id = XID.of(gtridData, bqualData, formatType)
        then:
        id != null
        id.getGlobalTransactionId() == null
        id.getBranchQualifier() == bqualData
        id.getFormatId() == formatType
    }

    def "Only gtrid data"()
    {
        setup:
        byte[] data = (0..XID.MAX_XID_DATA_SIZE/2 + 1) as byte[]
        def gtridLength = data.length
        def bqualLength = 0
        def formatType = 42l
        when:
        Xid id = XID.of(gtridLength, bqualLength, data, formatType)
        then:
        id != null
        id.getGlobalTransactionId() == data
        id.getBranchQualifier() == null
        id.getFormatId() == formatType
    }

    def "Only bqual data"()
    {
        setup:
        byte[] data = (0..XID.MAX_XID_DATA_SIZE/2 + 1) as byte[]
        def gtridLength = 0
        def bqualLength = data.length
        def formatType = 42l
        when:
        Xid id = XID.of(gtridLength, bqualLength, data, formatType)
        then:
        id != null
        id.getGlobalTransactionId() == null
        id.getBranchQualifier() == data
        id.getFormatId() == formatType
    }

    def "gtrid and bqual"()
    {
        setup:
        byte[] gtridData = (0..XID.MAX_XID_DATA_SIZE/2 -1) as byte[]
        byte[] bqualData = (0..XID.MAX_XID_DATA_SIZE/2 -1) as byte[]
        def formatType = 42l
        when:
        Xid id = XID.of(gtridData, bqualData, formatType)
        then:
        id != null
        id.getGlobalTransactionId() == gtridData
        id.getBranchQualifier() == bqualData
        id.getFormatId() == formatType
    }

    def "gtrid and bqual data"()
    {
        setup:
        byte[] gtridData = (0..XID.MAX_XID_DATA_SIZE/2 -1) as byte[]
        byte[] bqualData = (0..XID.MAX_XID_DATA_SIZE/2 -1) as byte[]
        byte[] data = ByteBuffer.allocate(gtridData.length + bqualData.length).put(gtridData).put(bqualData).array()
        def gtridLength = gtridData.length
        def bqualLength = bqualData.length
        def formatType = 42l
        when:
        Xid id = XID.of(gtridLength, bqualLength, data, formatType)
        then:
        id != null
        id.getGlobalTransactionId() == gtridData
        id.getBranchQualifier() == bqualData
        id.getFormatId() == formatType
    }

    def "Equals and hashCode"()
    {
        given:
        Random r = new Random(42 )
        byte[] gtridData1 = (0..XID.MAX_XID_DATA_SIZE/2 -1) as byte[]
        byte[] bqualData1 = (0..XID.MAX_XID_DATA_SIZE/2 -1) as byte[]
        byte[] gtridData2 = (0..XID.MAX_XID_DATA_SIZE/2 -1) as byte[]
        byte[] bqualData2 = (0..XID.MAX_XID_DATA_SIZE/2 -1) as byte[]
        def formatType = 42l

        r.nextBytes( gtridData1 )
        r.nextBytes( bqualData1 )

        when:
        Xid instance1 = XID.of(gtridData1, bqualData1, formatType)
        Xid instance2 = XID.of(gtridData1, bqualData1, formatType)
        Xid instance3 = XID.of(gtridData2, bqualData2, formatType)

        then:
        instance1 == instance1
        instance1 == instance2
        instance1 != instance3
        instance1.hashCode(  ) == instance1.hashCode(  )
        instance1.hashCode(  ) == instance2.hashCode(  )
        instance1.hashCode(  ) != instance3.hashCode(  )
        !instance1.equals( "String" )
    }

}
