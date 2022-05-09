/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.test.service.remote;

public class MissingResourceException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    public MissingResourceException()
    {
    }

    public MissingResourceException(String s)
    {
        super(s);
    }

    public MissingResourceException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public MissingResourceException(Throwable throwable)
    {
        super(throwable);
    }

    public MissingResourceException(String s, Throwable throwable, boolean b, boolean b1)
    {
        super(s, throwable, b, b1);
    }
}
