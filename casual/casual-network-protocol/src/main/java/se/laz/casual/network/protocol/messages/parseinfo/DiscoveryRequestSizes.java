/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.parseinfo;

/**
 * Created by aleph on 2017-02-23.
 */
public enum DiscoveryRequestSizes
{
    EXECUTION(16, 16),
    DOMAIN_ID(16, 16),
    // We restrict this internally to Integer.MAX_VALUE
    DOMAIN_NAME_SIZE(8, 8),
    SERVICES_SIZE(8, 8),
    // We restrict this internally to Integer.MAX_VALUE
    SERVICES_ELEMENT_SIZE(8,8),
    SERVICES_ELEMENT_DATA(128, 128),
    QUEUES_SIZE(8,8),
    QUEUES_ELEMENT_SIZE(8,8),
    QUEUES_ELEMENT_DATA(128, 128);

    private final int nativeSize;
    private final int networkSize;
    DiscoveryRequestSizes(int nativeSize, int networkSize)
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
