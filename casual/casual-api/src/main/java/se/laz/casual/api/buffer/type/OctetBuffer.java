/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.buffer.type;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.CasualBufferType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OctetBuffer implements CasualBuffer
{
    private static final long serialVersionUID = 1L;
    private final List<byte[]> data;
    private OctetBuffer(List<byte[]> data)
    {
        this.data = data;
    }

    public static OctetBuffer of(byte[] data)
    {
        Objects.requireNonNull(data, "data can not be null");
        List<byte[]> l = new ArrayList<>();
        l.add(data);
        return of(l);
    }

    public static OctetBuffer of(List<byte[]> data)
    {
        Objects.requireNonNull(data, "data can not be null");
        return new OctetBuffer(data);
    }

    @Override
    public String getType()
    {
        return CasualBufferType.X_OCTET.getName();
    }

    @Override
    public List<byte[]> getBytes()
    {
        return Collections.unmodifiableList(data);
    }
}
