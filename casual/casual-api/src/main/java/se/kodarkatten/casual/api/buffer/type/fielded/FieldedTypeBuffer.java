package se.kodarkatten.casual.api.buffer.type.fielded;

import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.buffer.CasualBufferType;
import se.kodarkatten.casual.api.buffer.type.fielded.impl.FieldedDataImpl;
import se.kodarkatten.casual.api.buffer.type.fielded.json.CasualField;
import se.kodarkatten.casual.api.buffer.type.fielded.json.CasualFieldedLookup;
import se.kodarkatten.casual.api.buffer.type.fielded.json.CasualFieldedLookupException;

import java.util.*;


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

    public FieldedData<?> getForName(final String name)
    {
        return getForName(name, 0);
    }

    public <T> FieldedTypeBuffer write(final String name, final T value)
    {
        final CasualField f = CasualFieldedLookup.forName(name).orElseThrow(() -> new CasualFieldedLookupException("name: " + name + " does not exist"));
        final Class<?> clazz = f.getType().getClazz();
        final Class<?> valueClazz = value.getClass();
        if(!clazz.equals(valueClazz))
        {
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

    private FieldedData<?> getForName(String name, int index)
    {
        List<FieldedData<?>> l = m.get(name);
        if(null == l)
        {
            throw new CasualFieldedLookupException("name: " + name + " does not exist with index: " + index);
        }
        return l.get(index);
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
}
