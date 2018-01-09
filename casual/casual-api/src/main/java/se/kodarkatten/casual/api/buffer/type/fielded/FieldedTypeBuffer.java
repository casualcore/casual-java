package se.kodarkatten.casual.api.buffer.type.fielded;

import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.buffer.CasualBufferType;
import se.kodarkatten.casual.api.buffer.type.fielded.impl.FieldedDataImpl;
import se.kodarkatten.casual.api.buffer.type.fielded.json.CasualField;
import se.kodarkatten.casual.api.buffer.type.fielded.json.CasualFieldedLookup;
import se.kodarkatten.casual.api.buffer.type.fielded.json.CasualFieldedLookupException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;


// "Generic wildcard types should not be used in return parameters"
// This is for framework use only, no user will ever use this code
// So no risk of confusion
@SuppressWarnings("squid:S1452")
public final class FieldedTypeBuffer implements CasualBuffer
{
    private Map<String, List<FieldedData<?>>> m;
    private FieldedTypeBuffer(final Map<String, List<FieldedData<?>>> m)
    {
        this.m = m;
    }

    /**
     *
     * @param l - fielded data
     * @return A buffer from which values can be read by name
     */
    public static FieldedTypeBuffer create(final List<byte[]> l)
    {
        Objects.requireNonNull(l, "buffer is not allowed to be null");
        if(l.isEmpty())
        {
            throw new CasualFieldedLookupException("list is empty, no way to handle this");
        }
        return new FieldedTypeBuffer(FieldedTypeBufferDecoder.decode(l));
    }

    /**
     * Use this to create an empty buffer that you want to write to
     * @return
     */
    public static FieldedTypeBuffer create()
    {
        return new FieldedTypeBuffer(new HashMap<>());
    }

    public static FieldedTypeBuffer of(FieldedTypeBuffer b)
    {
        Objects.requireNonNull(b, "buffer can not be null");
        FieldedTypeBuffer r = FieldedTypeBuffer.create();
        // shallow copy, keys and values are immutable
        b.m.forEach((key, val) -> r.m.put(key, val.stream().collect(Collectors.toList())));
        return r;
    }

    public List<byte[]> encode()
    {
        return FieldedTypeBufferEncoder.encode(m);
    }

    /**
     * Extract the internal representation setting the state of this buffer to empty
     * @return
     */
    public Map<String, List<FieldedData<?>>> extract()
    {
        Map<String, List<FieldedData<?>>> r = m;
        m = new HashMap<>();
        return r;
    }

    public boolean isEmpty()
    {
        return m.isEmpty();
    }

    /**
     * Clears the current fielded data and replaces it with the incoming data
     * Uses the {@link #writeAll(String, List) writeAll} method
     * This is to verify that the data is correct in case it was created/manipulated outside of a FieldedTypeBuffer
     * It also ensures that we do not keep any references to anything mutable
     * @param d
     * @return
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

    public FieldedData<?> read(long realId)
    {
        return read(realId, 0);
    }

    public FieldedData<?> read(long realId, boolean remove)
    {
        return read(realId, 0, remove);
    }

    public FieldedData<?> read(long realId, int index)
    {
        return read(realId, index, false);
    }

    public FieldedData<?> read(long realId, int index, boolean remove)
    {
        CasualField f = CasualFieldedLookup.forRealId(realId).orElseThrow(() -> new CasualFieldedLookupException("realId: " + realId + " does not exist"));
        return read(f.getName(), index, remove);
    }

    public FieldedData<?> read(final String name)
    {
        return read(name, 0);
    }

    public FieldedData<?> read(final String name, boolean remove)
    {
        return read(name, 0, remove);
    }

    public FieldedData<?> read(String name, int index)
    {
        return read(name, index, false);
    }

    public FieldedData<?> read(String name, int index, boolean remove)
    {
        Optional<FieldedData<?>> d = peek(name, index);
        if(remove && d.isPresent())
        {
            return remove(name, index);
        }
        return d.orElseThrow(createNameMissingException(name, Optional.of(index)));
    }


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

    public Optional<FieldedData<?>> peek(String name)
    {
        return peek(name, 0, false);
    }

    public Optional<FieldedData<?>> peek(String name, int index)
    {
        return peek(name, index, false);
    }

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
     * Note, in case name is not found - returns an empty list
     * @param name
     * @return List with data or if name is not found, and empty list
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

    public <T> FieldedTypeBuffer writeAll(final String name, final List<T> values)
    {
        for(T v : values)
        {
            write(name, v);
        }
        return this;
    }

    public <T> FieldedTypeBuffer write(final String name, final T value)
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

    public static Supplier<CasualFieldedLookupException> createNameMissingException(String name, Optional<Integer> index)
    {
        StringBuilder b = new StringBuilder();
        b.append("name: ");
        b.append(name);
        b.append(" does not exist with index: ");
        b.append(index.orElse(0));
        return () -> new CasualFieldedLookupException(b.toString());
    }

    public static Supplier<CasualFieldedLookupException> createIndexOutOfBoundException(String name, Integer index)
    {
        return () -> new CasualFieldedLookupException("index out of bounds index: " + index + " for name: " + name);
    }

}
