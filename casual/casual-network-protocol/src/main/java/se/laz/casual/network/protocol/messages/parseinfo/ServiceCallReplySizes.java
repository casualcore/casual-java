/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.parseinfo;

/**
 * Created by aleph on 2017-02-23.
 */
public enum ServiceCallReplySizes
{
    CALL_ERROR(4, 4),
    CALL_CODE(8, 8),
    TRANSACTION_STATE(8, 1),
    BUFFER_TYPE_NAME_SIZE(8, 8),
    BUFFER_PAYLOAD_SIZE(8, 8);

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
