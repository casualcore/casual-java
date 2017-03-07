package se.kodarkatten.casual.network.utils

/**
 * Created by aleph on 2017-03-03.
 */
final class ResourceLoader
{
    private ResourceLoader()
    {}
    static byte[] getResourceAsByteArray(String r)
    {
        InputStream s = ResourceLoader.class.getResourceAsStream(r)
        return s.getBytes()
    }
}
