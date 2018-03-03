/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.parseinfo;

/**
 * Created by aleph on 2017-02-23.
 */
public enum ServiceCallReplySizes
{
    EXECUTION(16, 16),
    CALL_ERROR(4, 4),
    CALL_CODE(8, 8),
    TRANSACTION_TRID_XID_FORMAT(8, 8),
    TRANSACTION_TRID_XID_GTRID_LENGTH(8, 8),
    TRANSACTION_TRID_XID_BQUAL_LENGTH(8, 8),
    // byte array with the size of gtrid_length + bqual_length (max 128)
    TRANSACTION_TRID_XID_PAYLOAD(32, 32),
    TRANSACTION_STATE(8, 1),
    BUFFER_TYPE_NAME_SIZE(8, 8),
    BUFFER_TYPE_NAME_DATA(8, 8),
    BUFFER_TYPE_SUBNAME_SIZE(8, 8),
    BUFFER_TYPE_SUBNAME_DATA(16, 16),
    BUFFER_PAYLOAD_SIZE(8, 8),
    BUFFER_PAYLOAD_DATA(128, 128);

    private final int nativeSize;
    private final int networkSize;
    private ServiceCallReplySizes(int nativeSize, int networkSize)
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
