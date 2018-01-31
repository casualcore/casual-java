package se.kodarkatten.casual.api.buffer;

import java.io.Serializable;
import java.util.List;

/**
 * @author jone
 */
public interface CasualBuffer extends Serializable
{
    String getType();
    List<byte[]> getBytes();
}
