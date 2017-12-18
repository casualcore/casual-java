package se.kodarkatten.casual.api.buffer.type.fielded.mapper;

public interface CasualObjectMapper<S extends Object, D extends Object>
{
    D to(S src);
    S from(D dst);
    Class<?> getDstType();
}