/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.buffer;

import se.laz.casual.api.CasualRuntimeException;

public class InboundRequestException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;

    public InboundRequestException(String message)
    {
        super(message);
    }
}
