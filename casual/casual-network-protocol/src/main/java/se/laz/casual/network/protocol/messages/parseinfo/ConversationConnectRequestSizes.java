/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.parseinfo;

/**
 * Created by aleph on 2017-02-23.
 */
public enum ConversationConnectRequestSizes
{
    SERVICE_NAME_SIZE(8, 8),
    SERVICE_TIMEOUT(8, 8),
    PARENT_SPAN_SIZE(8, 8),
    PARENT_NAME_SIZE(8, 8),
    DUPLEX(2, 2),
    BUFFER_TYPE_NAME_SIZE(8, 8),
    BUFFER_PAYLOAD_SIZE(8, 8),
    HAS_VALUE(1,1);

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
