package se.kodarkatten.casual.api.external.json;

import java.io.Reader;

public interface JsonProvider
{
    <T> T fromJson(final Reader r, Class<T> clazz);
    <T> T fromJson(final String s, Class<T> clazz);
    <T> T fromJson(final String s, Class<T> clazz, Object typeAdapter );
    String toJson( Object object );

}
