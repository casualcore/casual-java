package se.kodarkatten.casual.api.xa;

import se.kodarkatten.casual.api.xa.exceptions.XIDException;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by aleph on 2017-03-14.
 */
public final class XID
{
    private static final int XID_DATA_SIZE = 128;
    // default to null format
    private final XIDFormatType formatType;
    // length of the transaction gtrid part
    private final int gtridLength;
    // length of the transaction branch part
    private final int bqualLength;
    // (size = gtridLength + bqualLength) <= XID_DATA_SIZE
    private final byte[] data;
    private XID(int gtridLength, int bqualLength, final byte[] data, final XIDFormatType formatType)
    {
        this.gtridLength = gtridLength;
        this.bqualLength = bqualLength;
        this.data = data;
        this.formatType = formatType;
    }

    private XID()
    {
        this(0, 0, null, XIDFormatType.NULL);
    }

    /**
     * Null XID
     * @return
     */
    public static XID of()
    {
        return new XID();
    }

    public static XID of(final XID xid)
    {
        if(xid.formatType == XIDFormatType.NULL)
        {
            return XID.of();
        }
        return XID.of(xid.gtridLength, xid.bqualLength, Arrays.copyOf(xid.data, xid.data.length), xid.formatType);
    }

    public static XID of(int gtridLength, int bqualLength, final byte[] data, final XIDFormatType formatType)
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
        return new XID(gtridLength, bqualLength, data, formatType);
    }

    private static boolean isNullFormatTypeButAdditionalInformationProvided(int gtridLength, int bqualLength, final byte[] data, final XIDFormatType formatType)
    {
        if(XIDFormatType.NULL != formatType)
        {
            return false;
        }
        return gtridLength > 0 || bqualLength > 0 || (null != data && data.length > 0);
    }

    public XIDFormatType getFormatType()
    {
        return formatType;
    }

    public int getGtridLength()
    {
        return gtridLength;
    }

    public int getBqualLength()
    {
        return bqualLength;
    }

    public byte[] getData()
    {
        return Arrays.copyOf(data, data.length);
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
            Arrays.equals(data, xid.data);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(formatType, gtridLength, bqualLength, data);
    }
}
