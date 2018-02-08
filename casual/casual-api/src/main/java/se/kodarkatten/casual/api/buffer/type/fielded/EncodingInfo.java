package se.kodarkatten.casual.api.buffer.type.fielded;

import java.nio.charset.Charset;
import java.util.logging.Logger;

public final class EncodingInfo
{
    private static final Logger log = Logger.getLogger(EncodingInfo.class.getName());
    private static final Charset charset;
    private EncodingInfo()
    {}
    static
    {
        EncodingInfoProvider p = EncodingInfoProvider.of();
        charset = p.getCharset();
        log.info(() -> "casual fielded encoding set to charset: " + charset);
    }
    public static Charset getCharset()
    {
        return charset;
    }
}
