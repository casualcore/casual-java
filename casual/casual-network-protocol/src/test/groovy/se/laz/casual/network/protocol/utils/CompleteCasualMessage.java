/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.utils;

import java.util.Objects;

public record CompleteCasualMessage(String base64Encoded)
{
    public CompleteCasualMessage
    {
        Objects.requireNonNull(base64Encoded, "encoded string can not be null");
    }
}
