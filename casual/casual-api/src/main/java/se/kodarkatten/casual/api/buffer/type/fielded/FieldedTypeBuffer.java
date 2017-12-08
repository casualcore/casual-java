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


public final class FieldedTypeBuffer implements CasualBuffer
{
    private final Map<String, List<FieldedData<?>>> m;
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

    public List<byte[]> encode()
    {
        return FieldedTypeBufferEncoder.encode(m);
    }

    /**
     * Note, this leaks out a reference to our internal representation
     * Use with extreme caution!!!
     * @return
     */
    public Map<String, List<FieldedData<?>>> getInternalRepresentation()
    {
        return m;
    }

    // "Generic wildcard types should not be used in return parameters"
    // This is for framework use only, no user will ever use this code
    // So no risk of confusion
    @SuppressWarnings("squid:S1452")
    public FieldedData<?> read(final String name)
    {
        return read(name, 0);
    }

    // "Generic wildcard types should not be used in return parameters"
    // This is for framework use only, no user will ever use this code
    // So no risk of confusion
    @SuppressWarnings("squid:S1452")
    public FieldedData<?> read(String name, int index)
    {
        List<FieldedData<?>> l = m.get(name);
        if(null == l)
        {
            throw createNameMissingException(name, Optional.of(index)).get();
        }
        return l.get(index);
    }

    // "Generic wildcard types should not be used in return parameters"
    // This is for framework use only, no user will ever use this code
    // So no risk of confusion
    @SuppressWarnings("squid:S1452")
    public List<FieldedData<?>> readAll(final String name)
    {
        List<FieldedData<?>> l = m.get(name);
        if(null == l)
        {
            throw new CasualFieldedLookupException("nothing found for: " + name);
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
            // maybe always go with a stringified version in case we don't support a type?
            throw new CasualFieldedLookupException("class: " + valueClazz + " is not compatible with field class: " + clazz);
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
}
