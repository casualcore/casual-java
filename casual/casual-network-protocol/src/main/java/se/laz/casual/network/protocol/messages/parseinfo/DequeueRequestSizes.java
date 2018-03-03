/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.parseinfo;

public enum DequeueRequestSizes
{
    NAME_SIZE(8, 8),
    SELECTOR_PROPERTIES_SIZE(8, 8),
    SELECTOR_ID_SIZE(16, 16),
    // boolean
    BLOCK(1,1);
    private final int nativeSize;
    private final int networkSize;
    DequeueRequestSizes(int nativeSize, int networkSize)
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
