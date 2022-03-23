/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.CasualBufferType;
import se.laz.casual.api.buffer.type.fielded.impl.FieldedDataImpl;
import se.laz.casual.api.buffer.type.fielded.json.CasualField;
import se.laz.casual.api.buffer.type.fielded.json.CasualFieldedLookup;
import se.laz.casual.api.buffer.type.fielded.json.CasualFieldedLookupException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A CasualBuffer of type {@link CasualBufferType#FIELDED}
 */
// "Generic wildcard types should not be used in return parameters"
// This is for framework use only, no user will ever use this code
// So no risk of confusion
@SuppressWarnings("squid:S1452")
public final class FieldedTypeBuffer implements CasualBuffer
{
    private static final long serialVersionUID = 1L;
    private Map<String, List<FieldedData<?>>> m;
    private boolean allowNullUseDefault = false;
    private FieldedTypeBuffer(final Map<String, List<FieldedData<?>>> m)
    {
        this.m = m;
    }

    /**
     * Creates a new buffer
     * @param l fielded encoded data
     * @return a new buffer
     */
    public static FieldedTypeBuffer create(final List<byte[]> l)
    {
        Objects.requireNonNull(l, "buffer is not allowed to be null");
        if(l.isEmpty())
        {
            return FieldedTypeBuffer.create();
        }
        return new FieldedTypeBuffer(FieldedTypeBufferDecoder.decode(l));
    }

    /**
     * Creates a new buffer that allows for writing default values when a user writes a null value
     *
     * @param l fielded encoded data
     * @return a new buffer
     */
    public static FieldedTypeBuffer createAllowNullUseDefault(final List<byte[]> l)
    {
        FieldedTypeBuffer buffer = create(l);
        buffer.allowNullUseDefault = true;
        return buffer;
    }

    /**
     * Creates a new empty buffer
     * @return a new empty buffer
     */
    public static FieldedTypeBuffer create()
    {
        return new FieldedTypeBuffer(new HashMap<>());
    }

    /**
     * Creates a new empty buffer that allows for writing default values when a user writes a null value
     * @return a new empty buffer
     */
    public static FieldedTypeBuffer createAllowNullUseDefault()
    {
        FieldedTypeBuffer buffer = create();
        buffer.allowNullUseDefault = true;
        return buffer;
    }

    /**
     * Creates a copy of a buffer
     * Note that there is no deep copying going on as the keys and values are immutable
     *
     * Allows for writing default values when a user writes a null value
     *
     * @param b the buffer to copy
     * @return a new buffer
     */
    public static FieldedTypeBuffer ofAllowNullUseDefault(FieldedTypeBuffer b)
    {
        FieldedTypeBuffer buffer = of(b);
        buffer.allowNullUseDefault = true;
        return buffer;
    }

    /**
     * Creates a copy of a buffer
     * Note that there is no deep copying going on as the keys and values are immutable
     * @param b the buffer to copy
     * @return a new buffer
     */
    public static FieldedTypeBuffer of(FieldedTypeBuffer b)
    {
        Objects.requireNonNull(b, "buffer can not be null");
        FieldedTypeBuffer r = FieldedTypeBuffer.create();
        // shallow copy, keys and values are immutable
        b.m.forEach((key, val) -> r.m.put(key, val.stream().collect(Collectors.toList())));
        return r;
    }

    /**
     * Encode this buffer
     * @return the fielded encoded representation
     */
    public List<byte[]> encode()
    {
        return FieldedTypeBufferEncoder.encode(m);
    }

    /**
     * Extracts the internal representation setting the state of this buffer to empty
     * @return the extracted data
     */
    public Map<String, List<FieldedData<?>>> extract()
    {
        Map<String, List<FieldedData<?>>> r = m;
        m = new HashMap<>();
        return r;
    }

    /**
     * Checks if the buffer is empty
     * @return true if empty, false if not
     */
    public boolean isEmpty()
    {
        return m.isEmpty();
    }

    /**
     * Clears the current fielded data and replaces it with the incoming data
     * Uses the {@link #writeAll(String, List) writeAll} method
     * This is to verify that the data is correct in case it was created/manipulated outside of a FieldedTypeBuffer
     * It also ensures that we do not keep any references to anything mutable
     * @param d the data
     * @return the same buffer but containing the new data
     */
    // squid:S1612 - sonar hates lambdas
    @SuppressWarnings("squid:S1612")
    public FieldedTypeBuffer replace(final Map<String, List<FieldedData<?>>> d)
    {
        m = new HashMap<>();
        d.forEach((key, value) ->
            writeAll(key, value.stream()
                               .map(v -> v.getData())
                               .collect(Collectors.toList())));
        return this;
    }

    /**
     * Reads the first element based on a {@code realId}
     * Non destructive
     * @throws CasualFieldedLookupException if the id does not exist or if index is out of bounds
     * @param realId the real id
     * @return the fielded data
     */
    public FieldedData<?> read(long realId)
    {
        return read(realId, 0);
    }

    /**
     * Reads the first element based on a {@code realId}
     * May be a destructive read depending on if {@code remove} is set to true
     * @throws CasualFieldedLookupException if the id does not exist or if index is out of bounds
     * @param realId the real id
     * @param remove true if the item should be removed ( destructive read )
     * @return the fielded data
     */
    public FieldedData<?> read(long realId, boolean remove)
    {
        return read(realId, 0, remove);
    }

    /**
     * Reads the element at {@code index} based on a {@code realId}
     * Non destructive
     * @throws CasualFieldedLookupException if the id does not exist or if index is out of bounds
     * @param realId the real id
     * @param index the index
     * @return the fielded data
     */
    public FieldedData<?> read(long realId, int index)
    {
        return read(realId, index, false);
    }

    /**
     * Reads the element at {@code index} based on a {@code realId}
     * May be a destructive read depending on if {@code remove} is set to true
     * @throws CasualFieldedLookupException if the id does not exist or if index is out of bounds
     * @param realId the real id
     * @param index the index
     * @param remove true if the item should be removed ( destructive read )
     * @return the fielded data
     */
    public FieldedData<?> read(long realId, int index, boolean remove)
    {
        CasualField f = CasualFieldedLookup.forRealId(realId).orElseThrow(() -> new CasualFieldedLookupException("realId: " + realId + " does not exist"));
        return read(f.getName(), index, remove);
    }

    /**
     * Reads the first element by {@code name}
     * Non destructive
     * @throws CasualFieldedLookupException if the name does not exist or if index is out of bounds
     * @param name the name
     * @return the fielded data
     */
    public FieldedData<?> read(final String name)
    {
        return read(name, 0);
    }

    /**
     * Reads the first element by {@code name}
     * May be a destructive read depending on if {@code remove} is set to true
     * @throws CasualFieldedLookupException if the name does not exist or if index is out of bounds
     * @param name the name
     * @param remove true if the item should be removed ( destructive read )
     * @return the fielded data
     */
    public FieldedData<?> read(final String name, boolean remove)
    {
        return read(name, 0, remove);
    }

    /**
     * Reads the element at {@code index } by {@code name}
     * Non destructive
     * @throws CasualFieldedLookupException if the name does not exist or if index is out of bounds
     * @param name the name
     * @param index the index
     * @return the fielded data
     */
    public FieldedData<?> read(String name, int index)
    {
        return read(name, index, false);
    }

    /**
     * Reads the element at {@code index } by {@code name}
     * May be a destructive read depending on if {@code remove} is set to true
     * @throws CasualFieldedLookupException if the name does not exist or if index is out of bounds
     * @param name the name
     * @param index the index
     * @param remove true if the item should be removed ( destructive read )
     * @return the fielded data
     */
    public FieldedData<?> read(String name, int index, boolean remove)
    {
        Optional<FieldedData<?>> d = peek(name, index);
        if(remove && d.isPresent())
        {
            return remove(name, index);
        }
        return d.orElseThrow(createNameMissingException(name, Optional.of(index)));
    }

    /**
     * Removes the item with {@code name} at {@code index}
     * @throws CasualFieldedLookupException if the name does not exist or if index is out of bounds
     * @param name the name
     * @param index the index
     * @return the item that was removed
     */
    public FieldedData<?> remove(String name, int index)
    {
        List<FieldedData<?>> l = m.get(name);
        if(null == l)
        {
            throw createNameMissingException(name, Optional.of(index)).get();
        }
        if(index >= l.size())
        {
            throw createIndexOutOfBoundException(name, index).get();
        }
        FieldedData<?> r =  l.remove(index);
        if(l.isEmpty())
        {
            m.remove(name);
        }
        return r;
    }

    /**
     * Peeks at index 0 with {@code name} - works the same as read except that it does not throw
     * Non destructive
     * @param name the name
     * @return {@code Optional.empty()} if non existent, otherwise contains the item
     */
    public Optional<FieldedData<?>> peek(String name)
    {
        return peek(name, 0, false);
    }

    /**
     * Peeks at {@code index} with {@code name} - works the same as read except that it does not throw
     * Non destructive
     * @param name the name
     * @param index the index
     * @return {@code Optional.empty()} if non existent, otherwise contains the item
     */
    public Optional<FieldedData<?>> peek(String name, int index)
    {
        return peek(name, index, false);
    }

    /**
     * Peeks at {@code index} with {@code name} - works the same as read except that it does not throw
     * May be a destructive read depending on if {@code remove} is set to true
     * @param name the name
     * @param index the index
     * @param remove true if the item should be removed ( destructive peek )
     * @return {@code Optional.empty()} if non existent, otherwise contains the item
     */
    public Optional<FieldedData<?>> peek(String name, int index, boolean remove)
    {
        List<FieldedData<?>> l = m.get(name);
        if(null == l)
        {
            return Optional.empty();
        }
        if(remove)
        {
            return index < l.size() ? Optional.of(remove(name, index)) : Optional.empty();
        }
        return index < l.size() ? Optional.of(l.get(index)) : Optional.empty();
    }


    public List<FieldedData<?>> readAll(final String name)
    {
        return readAll(name, false);
    }

    /**
     * Read all items by {@code name}
     * Note, in case name is not found - returns an empty list
     * @param name the name
     * @param remove true if the item should be removed ( destructive {@code readAll} )
     * @return the data or if name is not found, and empty list
     */
    public List<FieldedData<?>> readAll(final String name, boolean remove)
    {
        List<FieldedData<?>> l = m.get(name);
        if(null == l)
        {
            return new ArrayList<>();
        }
        if(remove)
        {
            m.remove(name);
        }
        return l.stream().collect(Collectors.toList());
    }

    private <T> FieldedTypeBuffer writeAll(final String name, final List<T> values)
    {
        for(T v : values)
        {
            writeListItem(name, v);
        }
        return this;
    }

    private <T> FieldedTypeBuffer writeListItem(final String name, final T value)
    {
        final CasualField f = CasualFieldedLookup.forName(name).orElseThrow(createNameMissingException(name, Optional.empty()));
        final Class<?> clazz = f.getType().getClazz();
        Class<?> valueClazz = value.getClass();
        if(!clazz.equals(valueClazz))
        {
            // int is widened to long
            if (valueClazz.equals(Integer.class) && clazz.equals(Long.class))
            {
                return write(name, ((Integer)value).longValue());
            }
            else
            {
                throw new CasualFieldedLookupException("class: " + valueClazz + " is not compatible with field class: " + clazz);
            }
        }
        if (!m.containsKey(f.getName()))
        {
            m.put(f.getName(), new ArrayList<>());
        }
        List<FieldedData<?>> lf = m.get(f.getName());
        lf.add(FieldedDataImpl.of(value, FieldType.unmarshall(valueClazz)));
        return this;
    }

    public FieldedTypeBuffer write(final String name, final Object value)
    {
        return writeMaybeAllowNull(name, value, Optional.of(value.getClass()));
    }

    public FieldedTypeBuffer write(final String name, final Integer value)
    {
        return writeMaybeAllowNull(name, value, Optional.of(Integer.class));
    }

    public FieldedTypeBuffer write(final String name, final Long value)
    {
        return writeMaybeAllowNull(name, value, Optional.of(Long.class));
    }

    public FieldedTypeBuffer write(final String name, final Short value)
    {
        return writeMaybeAllowNull(name, value, Optional.of(Short.class));
    }

    public FieldedTypeBuffer write(final String name, final Character value)
    {
        return writeMaybeAllowNull(name, value, Optional.of(Character.class));
    }

    public FieldedTypeBuffer write(final String name, final byte[] value)
    {
        return writeMaybeAllowNull(name, value, Optional.of(byte[].class));
    }

    public FieldedTypeBuffer write(final String name, final Float value)
    {
        return writeMaybeAllowNull(name, value, Optional.of(Float.class));
    }

    public FieldedTypeBuffer write(final String name, final Double value)
    {
        return writeMaybeAllowNull(name, value, Optional.of(Double.class));
    }

    public FieldedTypeBuffer write(final String name, final String value)
    {
        return writeMaybeAllowNull(name, value, Optional.of(String.class));
    }

    /**
     * Write {@code value} by {@code name} to buffer
     * Note that int is widened to long as int is not a {@link FieldType}
     * @throws CasualFieldedLookupException in case the {@code name} is unknown
     * @param name the name
     * @param value the value
     * @param <T> the type of the value
     * @return this buffer
     */
    private <T> FieldedTypeBuffer writeMaybeAllowNull(final String name, final T value, Optional<Class<?>> providedValueClazz)
    {
        T localValue = value;
        final CasualField f = CasualFieldedLookup.forName(name).orElseThrow(createNameMissingException(name, Optional.empty()));
        final Class<?> clazz = f.getType().getClazz();
        // We really want lazy evaluation here since value can be null
        @SuppressWarnings("squid:S1612")
        final Class<?> valueClazz = providedValueClazz.orElseGet(() -> value.getClass());
        final boolean isIntegerValue = valueClazz.equals(Integer.class);
        final FieldType fieldType = FieldType.unmarshall((isIntegerValue) ? Long.class : valueClazz);
        localValue = maybeDefaultValue(isIntegerValue, localValue, fieldType);
        if(!clazz.equals(valueClazz))
        {
            // int is widened to long
            if (valueClazz.equals(Integer.class) && clazz.equals(Long.class))
            {
                if(null == localValue)
                {
                    throw new NullPointerException("value is not allowed to be null");
                }
                return writeMaybeAllowNull(name, ((Integer)localValue).longValue(), Optional.empty());
            }
            else
            {
                throw new CasualFieldedLookupException("class: " + valueClazz + " is not compatible with field class: " + clazz);
            }
        }
        if (!m.containsKey(f.getName()))
        {
            m.put(f.getName(), new ArrayList<>());
        }
        List<FieldedData<?>> lf = m.get(f.getName());
        lf.add(FieldedDataImpl.of(localValue, fieldType));
        return this;
    }

    private <T> T maybeDefaultValue(boolean isIntegerValue, T theValue, FieldType fieldType)
    {
        if(null == theValue && allowNullUseDefault)
        {
            return isIntegerValue ? fieldType.defaultValueInteger() : fieldType.defaultValue();
        }
        return theValue;
    }

    @Override
    public String toString()
    {
        StringBuilder b = new StringBuilder();
        b.append("{");
        Object[] keys = m.keySet().toArray();
        for(int i = 0; i < keys.length; ++i)
        {
            b.append(keys[i]);
            b.append(":");
            b.append("[");
            List<FieldedData<?>> lf = m.get(keys[i]);
            for(int j = 0; j< lf.size(); ++j)
            {
                b.append(lf.get(j).getData());
                if(j != lf.size() - 1)
                {
                    b.append(",");
                }
            }
            b.append("]");
            if(i != keys.length - 1)
            {
                b.append(",");
            }
        }
        b.append("}");
        return b.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        FieldedTypeBuffer that = (FieldedTypeBuffer) o;
        return Objects.equals(m, that.m);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(m);
    }

    @Override
    public String getType()
    {
        return CasualBufferType.FIELDED.getName();
    }

    @Override
    public List<byte[]> getBytes()
    {
        return encode();
    }

    /**
     * Creates a {@code Supplier<CasualFieldedLookupException>}
     * with the message that the {@code name} is missing at {@code Optional<Integer>} index
     * Index is 0 if not supplied
     * @param name the name
     * @param index the index
     * @return a supplier of CasualFieldedLookupException
     */
    public static Supplier<CasualFieldedLookupException> createNameMissingException(String name, Optional<Integer> index)
    {
        StringBuilder b = new StringBuilder();
        b.append("name: ");
        b.append(name);
        b.append(" does not exist with index: ");
        b.append(index.orElse(0));
        return () -> new CasualFieldedLookupException(b.toString());
    }

    /**
     * Creates a {@code Supplier<CasualFieldedLookupException>}
     * with the message that {@code Optional<Integer>} index is out of bounds for  {@code name}
     * @param name the name
     * @param index the index
     * @return a supplier of CasualFieldedLookupException
     */
    public static Supplier<CasualFieldedLookupException> createIndexOutOfBoundException(String name, Integer index)
    {
        return () -> new CasualFieldedLookupException("index out of bounds index: " + index + " for name: " + name);
    }

}
