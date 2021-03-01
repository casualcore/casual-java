/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import se.laz.casual.api.CasualRuntimeException;

public class ConfigurationException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;

    public ConfigurationException()
    {
    }

    public ConfigurationException( String message )
    {
        super( message );
    }

    public ConfigurationException( Throwable t )
    {
        super( t );
    }

    public ConfigurationException( String message, Throwable t )
    {
        super( message, t );
    }
}
