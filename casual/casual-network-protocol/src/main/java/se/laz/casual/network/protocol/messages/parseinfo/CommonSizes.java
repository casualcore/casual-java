/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.parseinfo;

/**
 * Created by aleph on 2017-04-03.
 */
public enum CommonSizes
{
    EXECUTION(16, 16),
    UUID_ID(16, 16),
    SERVICE_BUFFER_TYPE_SIZE(8, 8),
    SERVICE_BUFFER_PAYLOAD_SIZE(8,8),
    XID_FORMAT(8, 8),
    XID_GTRID_LENGTH(8, 8),
    XID_BQUAL_LENGTH(8, 8),
    // byte array with the size of gtrid_length + bqual_length (max 128)
    XID_PAYLOAD(32, 32),
    TRANSACTION_RESOURCE_ID(4, 4),
    TRANSACTION_RESOURCE_FLAGS(8, 8),
    TRANSACTION_RESOURCE_STATE(4, 4);

    private final int nativeSize;
    private final int networkSize;
    CommonSizes(int nativeSize, int networkSize)
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
