/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual;

import se.laz.casual.api.service.CasualService;

import java.lang.annotation.Annotation;

public class CasualServiceLiteral implements CasualService
{
    private String name;
    private String category;

    public CasualServiceLiteral( String name, String category )
    {
        this.name = name;
        this.category = category;
    }

    @Override
    public String name()
    {
        return name;
    }

    @Override
    public String category()
    {
        return category;
    }

    @Override
    public Class<? extends Annotation> annotationType()
    {
        return CasualService.class;
    }
}
