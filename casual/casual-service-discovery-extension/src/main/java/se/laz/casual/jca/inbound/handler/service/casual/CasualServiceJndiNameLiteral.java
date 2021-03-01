/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual;

import se.laz.casual.api.service.CasualServiceJndiName;

import java.lang.annotation.Annotation;

public class CasualServiceJndiNameLiteral implements CasualServiceJndiName
{
    private String value;

    public CasualServiceJndiNameLiteral( String value )
    {
        this.value = value;
    }

    @Override
    public String value()
    {
        return value;
    }

    @Override
    public Class<? extends Annotation> annotationType()
    {
        return CasualServiceJndiName.class;
    }
}
