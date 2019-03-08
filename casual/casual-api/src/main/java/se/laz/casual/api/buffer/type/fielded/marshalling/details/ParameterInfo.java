/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.marshalling.details;

import java.util.Objects;

public class ParameterInfo
{
    private final Class<?> type;
    protected ParameterInfo(final Class<?> type)
    {
        this.type = type;
    }

    /**
     * Creates a ParameterInfo instance
     * @param type the type
     * @return a new ParameterInfo instance
     */
    public static ParameterInfo of(final Class<?> type)
    {
        Objects.requireNonNull(type);
        return new ParameterInfo(type);
    }

    /**
     * @return the type
     */
    public Class<?> getType()
    {
        return type;
    }
}
