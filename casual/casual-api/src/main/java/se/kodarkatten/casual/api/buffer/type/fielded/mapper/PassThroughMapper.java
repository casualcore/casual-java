package se.kodarkatten.casual.api.buffer.type.fielded.mapper;

/**
 * The default object mapper
 */
public final class PassThroughMapper implements CasualObjectMapper<Object, Object>
{
    @Override
    public Object to(Object src)
    {
        return src;
    }

    @Override
    public Object from(Object dst)
    {
        return dst;
    }

    @Override
    public Class<?> getDstType()
    {
        return Object.class;
    }
}
