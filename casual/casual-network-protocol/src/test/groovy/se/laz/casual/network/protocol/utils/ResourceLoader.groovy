/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.utils

/**
 * Created by aleph on 2017-03-03.
 */
final class ResourceLoader
{
    private ResourceLoader()
    {}
    /**
     * Convience function
     * Not, max byte length is 8192
     * @param r
     * @return
     */
    static byte[] getResourceAsByteArray(String r)
    {
        InputStream s = ResourceLoader.class.getResourceAsStream(r)
        return s.getBytes()
    }
}
