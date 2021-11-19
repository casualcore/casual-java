/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.util;

import javax.transaction.xa.Xid;
import java.math.BigInteger;
import java.util.UUID;

public final class PrettyPrinter
{
    private PrettyPrinter()
    {}

    public static String casualStringify(Xid xid)
    {
        return String.format("%s:%s:%s",toHex(xid.getGlobalTransactionId()), toHex(xid.getBranchQualifier()), xid.getFormatId());
    }

    public static String casualStringify(UUID uuid)
    {
        return String.format("%x%x", uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    private static String toHex(byte[] bytes)
    {
        BigInteger val = new BigInteger(1, bytes);
        return String.format("%x", val);
    }

}
