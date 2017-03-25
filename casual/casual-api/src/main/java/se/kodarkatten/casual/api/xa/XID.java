package se.kodarkatten.casual.api.xa;

import se.kodarkatten.casual.api.xa.exceptions.XIDException;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import javax.transaction.xa.Xid;

/**
 * Created by aleph on 2017-03-14.
 */
public final class XID implements Xid
{
    private static final int XID_DATA_SIZE = 128;
    // default to null format
    private final long formatType;
    // length of the transaction gtrid part
    private final int gtridLength;
    // length of the transaction branch part
    private final int bqualLength;
    // (size = gtridLength + bqualLength) <= XID_DATA_SIZE
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

    /**
     * Null XID
     * @return
     */
    public static Xid of()
    {
        return new XID();
    }

    public static Xid of(final Xid xid)
    {
        if(XIDFormatType.isNullType(xid.getFormatId()))
        {
            return XID.of();
        }
        final byte[] gtridId = xid.getGlobalTransactionId();
        final byte[] bqual = xid.getBranchQualifier();
        return new XID(gtridId.length, bqual.length, xid.getFormatId(), gtridId, bqual);
    }

    public static Xid of(int gtridLength, int bqualLength, final byte[] data, final long formatType)
    {
        if((gtridLength + bqualLength) != data.length)
        {
            throw new XIDException("(gtridLength + bqualLength) != data.length " + "(" + gtridLength + " + " + bqualLength + ") != " + data.length);
        }
        else if(data.length > XID_DATA_SIZE)
        {
            throw new XIDException("data.length > XID_DATA_SIZE " + data.length + " > " + XID_DATA_SIZE);
        }
        else if(isNullFormatTypeButAdditionalInformationProvided(gtridLength, bqualLength, data, formatType))
        {
            throw new XIDException("You can not create a NULL XID with: gtridLength: " + gtridLength + " bqualLength: " + bqualLength + " data: " + data);
        }
        final byte[] globalTransactionId = Arrays.copyOf(data, gtridLength);
        final byte[] branchQualifier = Arrays.copyOfRange(data, gtridLength, gtridLength + bqualLength);
        return new XID(gtridLength, bqualLength, formatType, globalTransactionId, branchQualifier);
    }

    private static boolean isNullFormatTypeButAdditionalInformationProvided(int gtridLength, int bqualLength, final byte[] data, final long formatType)
    {
        if(!XIDFormatType.isNullType(formatType))
        {
            return false;
        }
        return gtridLength > 0 || bqualLength > 0 || (null != data && data.length > 0);
    }

    /**
     * globalTransactionId + branchQualifier
     * @return
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

    @Override
    public int hashCode()
    {
        return Objects.hash(formatType, gtridLength, bqualLength, globalTransactionId, branchQualifier);
    }

    @Override
    public int getFormatId()
    {
        return (int)formatType;
    }

    @Override
    public byte[] getGlobalTransactionId()
    {
        return Arrays.copyOf(globalTransactionId, globalTransactionId.length);
    }

    @Override
    public byte[] getBranchQualifier()
    {
        return Arrays.copyOf(branchQualifier, branchQualifier.length);
    }
}
