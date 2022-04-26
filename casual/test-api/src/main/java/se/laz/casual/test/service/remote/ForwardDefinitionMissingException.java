/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.test.service.remote;

public class ForwardDefinitionMissingException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    public ForwardDefinitionMissingException()
    {
        super();
    }

    public ForwardDefinitionMissingException(String message)
    {
        super(message);
    }

    public ForwardDefinitionMissingException(Throwable t)
    {
        super(t);
    }

    public ForwardDefinitionMissingException(String message, Throwable t)
    {
        super(message, t);
    }
}
