/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network;

import java.io.Serializable;
import java.util.UUID;

public final class CasualDecoderException extends RuntimeException implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final UUID corrid;
    public CasualDecoderException(Throwable t, UUID corrid)
    {
        super(t);
        this.corrid = corrid;
    }

    public UUID getCorrid()
    {
        return corrid;
    }
}
