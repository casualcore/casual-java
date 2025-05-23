/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.otel;

import java.util.UUID;

public class UUIDConverter
{
    private static final int EXPECTED_LENGTH = 32;
    private UUIDConverter()
    {}
    public static UUID convertTraceIdHexToUUID(String traceIdHex)
    {
        if (traceIdHex.length() != EXPECTED_LENGTH)
        {
            throw new IllegalArgumentException("Expected a 32 character trace id");
        }
        // Insert dashes to match the 8-4-4-4-12 UUID format.
        String uuidString = traceIdHex.substring(0, 8) + "-" +
                traceIdHex.substring(8, 12) + "-" +
                traceIdHex.substring(12, 16) + "-" +
                traceIdHex.substring(16, 20) + "-" +
                traceIdHex.substring(20, 32);
        return UUID.fromString(uuidString);
    }


}
