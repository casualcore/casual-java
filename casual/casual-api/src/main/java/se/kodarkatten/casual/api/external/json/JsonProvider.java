package se.kodarkatten.casual.api.external.json;

import java.io.Reader;

@FunctionalInterface
public interface JsonProvider
{
    <T> T fromJson(final Reader r, Class<T> clazz);
}
