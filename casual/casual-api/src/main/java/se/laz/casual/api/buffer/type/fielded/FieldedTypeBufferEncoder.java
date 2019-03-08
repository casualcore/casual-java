/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded;

import se.laz.casual.api.buffer.type.fielded.json.CasualField;
import se.laz.casual.api.buffer.type.fielded.json.CasualFieldedLookup;
import se.laz.casual.api.buffer.type.fielded.json.CasualFieldedLookupException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The{@code FieldedTypeBuffer} encoder
 */
public final class FieldedTypeBufferEncoder
{
    private FieldedTypeBufferEncoder()
    {}

    /**
     * Encodes the data
     * @param data - the data to be encoded
     * @return the encoded data
     */
    public static List<byte[]> encode(final Map<String, List<FieldedData<?>>> data)
    {
        final List<byte[]> l = new ArrayList<>();
        for(final Map.Entry<String, List<FieldedData<?>>> item : data.entrySet())
        {
            l.addAll(encodeItem(item));
        }
        return l;
    }

    private static List<byte[]> encodeItem(final Entry<String, List<FieldedData<?>>> item)
    {
        final String name = item.getKey();
        final List<byte[]> l = new ArrayList<>();
        for(final FieldedData<?> d : item.getValue())
        {

            final CasualField f = CasualFieldedLookup.forName(name).orElseThrow(() -> new CasualFieldedLookupException("name: " + name + " does not exist"));
            final byte[] bytes = getBytes(d, f.getType());
            final ByteBuffer b = ByteBuffer.allocate(FieldSize.FIELD_ID.getSize() + FieldSize.FIELD_SIZE.getSize() + bytes.length);
            b.putLong(f.getRealId());
            b.putLong(bytes.length);
            b.put(bytes);
            l.add(b.array());
        }
        return l;
    }

    private static byte[] getBytes(final FieldedData<?> d, FieldType type)
    {
        ByteBuffer b;
        switch(type)
        {
            case CASUAL_FIELD_SHORT:
                b = ByteBuffer.allocate(Short.BYTES);
                b.putShort((Short) d.getData());
                break;
            case CASUAL_FIELD_LONG:
                b = ByteBuffer.allocate(Long.BYTES);
                b.putLong((Long) d.getData());
                break;
            case CASUAL_FIELD_CHAR:
                b = ByteBuffer.allocate(Byte.BYTES);
                char c = d.getData(Character.class);
                b.put((byte)c);
                break;
            case CASUAL_FIELD_FLOAT:
                b = ByteBuffer.allocate(Float.BYTES);
                b.putFloat((Float)d.getData());
                break;
            case CASUAL_FIELD_DOUBLE:
                b = ByteBuffer.allocate(Double.BYTES);
                b.putDouble((Double)d.getData());
                break;
            case CASUAL_FIELD_STRING:
                // casual expects a cstring, ie nullterminated data
                String v = d.getData() + "\0";
                return v.getBytes(EncodingInfo.getCharset());
            case CASUAL_FIELD_BINARY:
                return (byte[])d.getData();
            default:
                throw new CasualFieldedLookupException("type: " + type + " not supported");
        }
        return b.array();
    }


}
