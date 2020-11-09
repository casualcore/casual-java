/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides the encoding information for encoding/decoding strings
 * Defaults to UTF-8 but can be set via the environment variable {@code CASUAL_API_FIELDED_ENCODING}
 */
public class EncodingInfoProvider
{
    private static final Logger log = Logger.getLogger(EncodingInfoProvider.class.getName());
    public static final String FIELDED_ENCODING_ENV_NAME = "CASUAL_API_FIELDED_ENCODING";
    private EncodingInfoProvider()
    {}

    /**
     * @return creates a new EncodingInfoProvider
     */
    public static EncodingInfoProvider of()
    {
        return new EncodingInfoProvider();
    }

    /**
     * @return the charset to use for string encoding/decoding
     */
    public Charset getCharset()
    {
        final String name = getEncoding().orElse(StandardCharsets.UTF_8.name());
        Charset c;
        try
        {
            c = Charset.forName(name);
        }
        catch(IllegalArgumentException e)
        {
            log.log(Level.WARNING, e, () -> "could not find charset by name: " + name + " falling back to utf-8");
            c = StandardCharsets.UTF_8;
        }
        return c;
    }

    /**
     * @return the name of the encoding to use if set via the environment variable
     */
    public static Optional<String> getEncoding()
    {
        return Optional.ofNullable(System.getenv(FIELDED_ENCODING_ENV_NAME));
    }

}
