/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.parseinfo;

/**
 * Created by aleph on 2017-02-23.
 */
public enum ServiceCallRequestSizes
{
    EXECUTION(16, 16),
    CALL_DESCRIPTOR(4, 8),
    SERVICE_NAME_SIZE(8, 8),
    SERVICE_NAME_DATA(128, 128),
    SERVICE_TIMEOUT(8, 8),
    PARENT_NAME_SIZE(8, 8),
    PARENT_NAME_DATA(128, 128),
    XID_FORMAT(8, 8),
    XID_GTRID_LENGTH(8, 8),
    XID_BQUAL_LENGTH(8, 8),
    // This one needs to be calculated at runtime?
    // gdrid_length + bqual_length <= 128
    XID_PAYLOAD(32, 32),
    FLAGS(8, 8),
    BUFFER_TYPE_NAME_SIZE(8, 8),
    BUFFER_TYPE_NAME_DATA(8, 8),
    BUFFER_TYPE_SUBNAME_SIZE(8, 8),
    BUFFER_TYPE_SUBNAME_DATA(16, 16),
    BUFFER_PAYLOAD_SIZE(8, 8),
    BUFFER_PAYLOAD_DATA(128, 128);

    private final int nativeSize;
    private final int networkSize;
    private ServiceCallRequestSizes(int nativeSize, int networkSize)
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
