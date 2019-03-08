/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded;

import java.nio.charset.Charset;
import java.util.logging.Logger;

/**
 * Encoding information to use for string encoding/decoding
 * @see EncodingInfoProvider
 */
public final class EncodingInfo
{
    private static final Logger log = Logger.getLogger(EncodingInfo.class.getName());
    private static final Charset charset;
    private EncodingInfo()
    {}
    static
    {
        EncodingInfoProvider p = EncodingInfoProvider.of();
        charset = p.getCharset();
        log.info(() -> "casual fielded encoding set to charset: " + charset);
    }

    /**
     * @return the charset to use for string encoding/decoding
     */
    public static Charset getCharset()
    {
        return charset;
    }
}
