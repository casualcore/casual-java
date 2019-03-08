/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.network.protocol.messages.exception;

import se.laz.casual.api.CasualRuntimeException;

/**
 * Exception that is used if anything outside of the protocol is seen
 * Created by aleph on 2017-02-23.
 */
public final class CasualProtocolException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;
    public CasualProtocolException(String msg)
    {
        super(msg);
    }
    public CasualProtocolException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
