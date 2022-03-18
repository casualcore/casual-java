/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;

import se.laz.casual.api.CasualRuntimeException;

public class ConversationConnectException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;

    public ConversationConnectException( )
    {
        super();
    }

    public ConversationConnectException(String message )
    {
        super(  message );
    }

    public ConversationConnectException(Throwable t )
    {
        super( t );
    }

    public ConversationConnectException(String message, Throwable t )
    {
        super( message, t );
    }

}
