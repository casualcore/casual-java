/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded;

/**
 * The serialized size for:
 * {@code FieldSize.FIELD_ID}
 * {@code FieldSize.FIELD_SIZE}
 */
public enum FieldSize
{
    FIELD_ID(8),
    FIELD_SIZE(8);
    private final int size;
    FieldSize(int size)
    {
        this.size = size;
    }
    public int getSize()
    {
        return size;
    }
}
