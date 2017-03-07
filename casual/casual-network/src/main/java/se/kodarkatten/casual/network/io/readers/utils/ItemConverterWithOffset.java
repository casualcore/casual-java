package se.kodarkatten.casual.network.io.readers.utils;

/**
 * Created by aleph on 2017-03-07.
 */
@FunctionalInterface
public interface ItemConverterWithOffset<T>
{
    T convertItem(final byte[] item, int offset, int length);
}

