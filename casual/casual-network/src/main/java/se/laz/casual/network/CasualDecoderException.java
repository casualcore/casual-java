/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network;

import se.laz.casual.api.CasualRuntimeException;

import java.util.UUID;

public final class CasualDecoderException extends CasualRuntimeException
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
