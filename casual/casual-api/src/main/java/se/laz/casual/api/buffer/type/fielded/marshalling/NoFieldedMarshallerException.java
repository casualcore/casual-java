/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.buffer.type.fielded.marshalling;
import se.laz.casual.api.CasualRuntimeException;
public class NoFieldedMarshallerException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1;
    public NoFieldedMarshallerException(String msg)
    {
        super(msg);
    }
    public NoFieldedMarshallerException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
