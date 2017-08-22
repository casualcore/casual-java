package se.kodarkatten.casual.api.buffer;

import java.util.List;

/**
 * @author jone
 */
public interface CasualBuffer
{
    String getType();
    List<byte[]> getPayload();
}
