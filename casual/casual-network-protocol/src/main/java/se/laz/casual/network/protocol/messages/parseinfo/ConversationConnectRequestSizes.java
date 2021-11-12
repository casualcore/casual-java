/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.parseinfo;

/**
 * Created by aleph on 2017-02-23.
 */
public enum ConversationConnectRequestSizes
{
    EXECUTION(16, 16),
    CALL_DESCRIPTOR(4, 8),
    SERVICE_NAME_SIZE(8, 8),
    SERVICE_TIMEOUT(8, 8),
    PARENT_NAME_SIZE(8, 8),
    XID_FORMAT(8, 8),
    XID_GTRID_LENGTH(8, 8),
    XID_BQUAL_LENGTH(8, 8),
    XID_PAYLOAD(32, 32),
    DUPLEX(2, 2),
    BUFFER_TYPE_NAME_SIZE(8, 8),
    BUFFER_PAYLOAD_SIZE(8, 8);

    private final int nativeSize;
    private final int networkSize;
    ConversationConnectRequestSizes(int nativeSize, int networkSize)
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
