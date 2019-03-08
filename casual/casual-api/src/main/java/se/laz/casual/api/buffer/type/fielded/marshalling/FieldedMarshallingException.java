/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.marshalling;

import se.laz.casual.api.CasualRuntimeException;

/**
 * Exception used if anything goes wrong during marshalling
 */
public class FieldedMarshallingException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1;
    public FieldedMarshallingException(String msg)
    {
        super(msg);
    }
    public FieldedMarshallingException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
