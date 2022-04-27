/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller.producer;

import se.laz.casual.api.qualifier.Casual;
import se.laz.casual.connection.caller.CasualCaller;
import se.laz.casual.connection.caller.CasualCallerException;

import javax.enterprise.inject.Produces;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class CasualCallerProducer
{
    @Casual
    @Produces
    public CasualCaller get()
    {
        try
        {
            InitialContext context = new InitialContext();
            return (CasualCaller) context.lookup("java:/global/casual-caller-app/casual-caller/CasualCallerImpl");
        }
        catch (NamingException e)
        {
            throw new CasualCallerException("Failed finding CasualCallerImpl, using CasualCaller will not work", e);
        }
    }
}