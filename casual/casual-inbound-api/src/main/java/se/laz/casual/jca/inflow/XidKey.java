/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;

import javax.transaction.xa.Xid;
import java.util.Arrays;
import java.util.Objects;

public class XidKey
{
    private final byte[] gtrid;
    private final byte[] bqual;
    private final int formatId;

    private XidKey(byte[] gtrid, byte[] bqual, int formatId)
    {
        this.gtrid = gtrid;
        this.bqual = bqual;
        this.formatId = formatId;
    }

    public static XidKey of(Xid xid)
    {
        Objects.requireNonNull(xid, "xid must not be null");
        byte[] gtrid = Arrays.copyOfRange(xid.getGlobalTransactionId(), 0, xid.getGlobalTransactionId().length);
        byte[] bqual = Arrays.copyOfRange(xid.getBranchQualifier(), 0, xid.getBranchQualifier().length);
        int formatId = xid.getFormatId();
        return new XidKey(gtrid, bqual, formatId);
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof XidKey xidKey))
        {
            return false;
        }
        return formatId == xidKey.formatId && Arrays.equals(gtrid, xidKey.gtrid) && Arrays.equals(bqual, xidKey.bqual);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(Arrays.hashCode(gtrid), Arrays.hashCode(bqual), formatId);
    }

    @Override
    public String toString()
    {
        return "XidKey{" +
                "gtrid=" + Arrays.toString(gtrid) +
                ", bqual=" + Arrays.toString(bqual) +
                ", formatId=" + formatId +
                '}';
    }
}
