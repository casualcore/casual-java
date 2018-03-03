/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.xa;

import se.laz.casual.api.CasualRuntimeException;

/**
 * Created by aleph on 2017-03-14.
 */
public class XIDException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1l;
    public XIDException(String msg)
    {
        super(msg);
    }
}
