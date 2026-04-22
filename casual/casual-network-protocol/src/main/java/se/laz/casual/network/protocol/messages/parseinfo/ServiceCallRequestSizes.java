/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.parseinfo;

/**
 * Created by aleph on 2017-02-23.
 */
public enum ServiceCallRequestSizes
{
    CALL_DESCRIPTOR(4, 8),
    SERVICE_NAME_SIZE(8, 8),
    HAS_VALUE(1,1),// since v1.3
    SERVICE_TIMEOUT(8, 8),
    PARENT_SPAN(8,8),// since v1.3
    PARENT_NAME_SIZE(8, 8),

    FLAGS(8, 8),
    BUFFER_TYPE_NAME_SIZE(8, 8),
    BUFFER_PAYLOAD_SIZE(8, 8);

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
