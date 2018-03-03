/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.parseinfo;

/**
 * Created by aleph on 2017-02-23.
 */
public enum RollbackRequestSizes
{
    EXECUTION(16, 16),
    XID_FORMAT(8, 8),
    XID_GTRID_LENGTH(8, 8),
    XID_BQUAL_LENGTH(8, 8),
    // byte array with the size of gtrid_length + bqual_length (max 128)
    XID_PAYLOAD(32, 32),
    RESOURCE_ID(4, 8),
    FLAGS(4, 8);

    private final int nativeSize;
    private final int networkSize;
    private RollbackRequestSizes(int nativeSize, int networkSize)
    {
        this.nativeSize = nativeSize;
        this.networkSize = networkSize;
    }
    public int getNativeSize()
    {
        return nativeSize;
    }
    public int getNetworkSize()
    {
        return networkSize;
    }
}
