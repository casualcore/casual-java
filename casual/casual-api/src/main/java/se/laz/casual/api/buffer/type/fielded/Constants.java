/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded;

/**
 * common fielded API constants
 */
public final class Constants
{
    private Constants()
    {}
    /**
     * The base type for fielded - internally used in encoding/decoding of fielded
     */
    public static final int CASUAL_FIELD_TYPE_BASE = 0x2000000;
    /**
     * Environment variable that should point to your json
     */
    public static final String CASUAL_FIELD_TABLE = "CASUAL_FIELD_TABLE";
    /**
     * Only used for internal testing, you should set the environment variable CASUAL_FIELD_TABLE to point to your json
     */
    public static final String CASUAL_FIELD_JSON_EMBEDDED = "casual-fields.json";
}
