/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;

import se.laz.casual.api.CasualRuntimeException;

public class ConversationDirectionException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;

    public ConversationDirectionException( )
    {
        super();
    }

    public ConversationDirectionException(String message )
    {
        super(  message );
    }

    public ConversationDirectionException(Throwable t )
    {
        super( t );
    }

    public ConversationDirectionException(String message, Throwable t )
    {
        super( message, t );
    }

}
