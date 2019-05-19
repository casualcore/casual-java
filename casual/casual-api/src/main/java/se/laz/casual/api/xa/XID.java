/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.xa;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * Created by aleph on 2017-03-14.
 */
public final class XID implements Xid
{
    public static final Xid NULL_XID = new XID();

    public static final int MAX_XID_DATA_SIZE = 128;
    // default to null format
    private final long formatType;
    // length of the transaction gtrid part
    private final int gtridLength;
    // length of the transaction branch part
    private final int bqualLength;
    // (size = gtridLength + bqualLength) <= MAX_XID_DATA_SIZE
    private final byte[] globalTransactionId;
    private final byte[] branchQualifier;
    private XID(int gtridLength, int bqualLength, long formatType, final byte[] globalTransactionId, final byte[] branchQualifier)
    {
        this.gtridLength = gtridLength;
        this.bqualLength = bqualLength;
        this.formatType = formatType;
        this.globalTransactionId = globalTransactionId;
        this.branchQualifier = branchQualifier;
    }

    private XID()
    {
        this(0, 0, XIDFormatType.NULL.getType(), null, null);
    }

    public static Xid of(final Xid xid)
    {
        if(XIDFormatType.isNullType(xid.getFormatId()))
        {
            return NULL_XID;
        }
        final byte[] gtridId = xid.getGlobalTransactionId();
        final byte[] bqual = xid.getBranchQualifier();
        return new XID(gtridId.length, bqual.length, xid.getFormatId(), gtridId, bqual);
    }

    public static Xid of(int gtridLength, int bqualLength, final byte[] data, long formatType)
    {
        Objects.requireNonNull(data, "data for xid can not be null!");
        if((gtridLength + bqualLength) != data.length)
        {
            throw new XIDException("(gtridLength + bqualLength) != data.length " + "(" + gtridLength + " + " + bqualLength + ") != " + data.length);
        }
        else if((gtridLength + bqualLength) > MAX_XID_DATA_SIZE)
        {
            throw new XIDException("xid total length > MAX_XID_DATA_SIZE " + (gtridLength + bqualLength) + " > " + MAX_XID_DATA_SIZE);
        }
        final byte[] globalTransactionId = (0 == gtridLength) ? null : Arrays.copyOf(data, gtridLength);
        final byte[] branchQualifier = (0 == bqualLength) ? null : Arrays.copyOfRange(data, gtridLength, gtridLength + bqualLength);
        return XID.of(globalTransactionId, branchQualifier, formatType);
    }

    public static Xid of(final byte[] globalTransactionId, final byte[] branchQualifier, final long formatType)
    {
        int gtridLength = (null != globalTransactionId) ? globalTransactionId.length : 0;
        int bqualLength = (null != branchQualifier) ? branchQualifier.length : 0;
        final int totalLength = gtridLength + bqualLength;
        if(totalLength > MAX_XID_DATA_SIZE)
        {
            throw new XIDException("xid total length > MAX_XID_DATA_SIZE " + totalLength + " > " + MAX_XID_DATA_SIZE);
        }
        else if(isNullFormatTypeButAdditionalInformationProvided(globalTransactionId, branchQualifier, formatType))
        {
            throw new XIDException("You can not create a NULL XID with: gtrid length " + gtridLength + " and bqual length " + bqualLength);
        }
        return new XID(gtridLength, bqualLength, formatType, globalTransactionId, branchQualifier);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        XID xid = (XID) o;
        return formatType == xid.formatType &&
                gtridLength == xid.gtridLength &&
                bqualLength == xid.bqualLength &&
                Arrays.equals(globalTransactionId, xid.globalTransactionId) &&
                Arrays.equals(branchQualifier, xid.branchQualifier);
    }

    /**
     * globalTransactionId + branchQualifier
     * @return the data, copied into a a new array
     */
    public byte[] getData()
    {
        final byte[] data = new byte[gtridLength + bqualLength];
        ByteBuffer b = ByteBuffer.wrap(data);
        b.put(globalTransactionId);
        b.put(branchQualifier);
        return b.array();
    }

    @Override
    public int hashCode()
    {
        String g = (globalTransactionId == null ) ? null : new String( globalTransactionId );
        String b = (branchQualifier == null ) ? null : new String( branchQualifier );
        return Objects.hash(formatType, gtridLength, bqualLength, g, b );
    }

    @Override
    public int getFormatId()
    {
        return (int)formatType;
    }

    @Override
    public byte[] getGlobalTransactionId()
    {
        return (null == globalTransactionId) ? null : Arrays.copyOf(globalTransactionId, globalTransactionId.length);
    }

    @Override
    public byte[] getBranchQualifier()
    {
        return (null == branchQualifier) ? null : Arrays.copyOf(branchQualifier, branchQualifier.length);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("XID{");
        sb.append("formatType=").append(formatType);
        sb.append(", gtridLength=").append(gtridLength);
        sb.append(", bqualLength=").append(bqualLength);
        sb.append(", globalTransactionId=").append(Arrays.toString(globalTransactionId));
        sb.append(", branchQualifier=").append(Arrays.toString(branchQualifier));
        sb.append('}');
        return sb.toString();
    }

    private static boolean isNullFormatTypeButAdditionalInformationProvided(byte[] globalTransactionId, byte[] branchQualifier, long formatType)
    {
        if(!XIDFormatType.isNullType(formatType))
        {
            return false;
        }
        return !(null == globalTransactionId && null == branchQualifier);
    }
}
